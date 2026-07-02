package app.dissipate.data.models;

import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * A single recorded change to an {@link Auditable} entity — the append-only audit + versioning log.
 * For federated entities the row also carries the stable {@code globalId}, the {@code originRegion},
 * and a {@code contentHash} of the snapshot; those make it the unit of cross-region reconciliation
 * (a later region can compare hashes to decide which revision wins). Rows are retained.
 */
@Entity
@Table(name = "entity_revisions")
public class EntityRevision extends DefaultPanacheEntityWithTimestamps {

  public static final String ID_GENERATOR_KEY = "EntityRevision";

  @Column(name = "entity_type", nullable = false)
  public String entityType;

  @Column(name = "entity_id", nullable = false)
  public Long entityId;

  /** Stable federation handle of the entity, or null for source-of-truth entities. */
  @Column(name = "global_id")
  public UUID globalId;

  /** Monotonic per-entity revision number, starting at 1. */
  @Column(nullable = false)
  public int revision;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public RevisionOp op;

  /** Who made the change (USER/APP), or null for system/anonymous changes. */
  @Enumerated(EnumType.STRING)
  @Column(name = "actor_kind")
  public PrincipalKind actorKind;

  @Column(name = "actor_id")
  public Long actorId;

  @Column(name = "changed_at", nullable = false)
  public Instant changedAt;

  /** SHA-256 (Base64) of the canonical snapshot; the cross-region reconciliation key. Null for local-only. */
  @Column(name = "content_hash")
  public String contentHash;

  @Column(name = "origin_region")
  public Integer originRegion;

  /** The entity's auditable state at this revision. */
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  public String snapshot;

  @Override
  @SuppressWarnings("unchecked")
  public Uni<EntityRevision> persistAndFlush() {
    return super.persistAndFlush();
  }

  /** The next revision number for an entity (1 if it has no revisions yet). */
  public static Uni<Integer> nextRevision(String entityType, Long entityId) {
    return find("entityType = ?1 and entityId = ?2 order by revision desc", entityType, entityId)
        .firstResult()
        .onItem().transform(latest -> latest == null ? 1 : ((EntityRevision) latest).revision + 1);
  }
}
