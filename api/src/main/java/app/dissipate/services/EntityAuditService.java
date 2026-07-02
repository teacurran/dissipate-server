package app.dissipate.services;

import app.dissipate.auth.Principal;
import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.models.Auditable;
import app.dissipate.data.models.EntityRevision;
import app.dissipate.data.models.RevisionOp;
import app.dissipate.utils.EncryptionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.Map;

/**
 * Records entity changes as {@link EntityRevision}s. Call it from within the same {@code @WithTransaction}
 * as the mutation so the revision commits atomically with the change. The snapshot is the entity's
 * {@link Auditable#auditSnapshot()} serialized canonically (keys sorted) so the content hash is stable
 * across nodes/regions — that hash is the cross-region reconciliation key for federated entities.
 */
@ApplicationScoped
public class EntityAuditService {

  /** Canonical JSON: map entries sorted by key so equal snapshots hash identically everywhere. */
  private static final ObjectMapper CANONICAL = JsonMapper.builder()
      .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
      .build();

  @Inject
  SnowflakeIdGenerator idGenerator;

  @Inject
  EncryptionUtil encryptionUtil;

  /** Record a revision of the given entity attributed to {@code actor} (null for system changes). */
  @WithSpan("EntityAuditService.record")
  public Uni<EntityRevision> record(Auditable entity, RevisionOp op, Principal actor) {
    String entityType = entity.entityType();
    Long entityId = entity.entityId();
    String snapshot = canonical(entity.auditSnapshot());

    return EntityRevision.nextRevision(entityType, entityId).onItem().transformToUni(revision -> {
      EntityRevision row = new EntityRevision();
      row.id = idGenerator.generate(EntityRevision.ID_GENERATOR_KEY);
      row.entityType = entityType;
      row.entityId = entityId;
      row.globalId = entity.auditGlobalId();
      row.revision = revision;
      row.op = op;
      if (actor != null && actor.isAuthenticated()) {
        row.actorKind = actor.kind();
        row.actorId = actor.meteredId();
      }
      row.changedAt = Instant.now();
      row.originRegion = entity.auditOriginRegion();
      row.snapshot = snapshot;
      row.contentHash = encryptionUtil.sha256(snapshot);
      return row.persistAndFlush();
    });
  }

  private static String canonical(Map<String, Object> snapshot) {
    try {
      return CANONICAL.writeValueAsString(snapshot);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("failed to serialize audit snapshot", e);
    }
  }
}
