package app.dissipate.services;

import app.dissipate.auth.Principal;
import app.dissipate.data.models.AccountRole;
import app.dissipate.data.models.Auditable;
import app.dissipate.data.models.EntityRevision;
import app.dissipate.data.models.PrincipalKind;
import app.dissipate.data.models.RevisionOp;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.vertx.VertxContextSupport;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Integration coverage for {@link EntityAuditService}: monotonic per-entity revisions, actor
 * attribution, federation fields carried through, and a content hash that changes with content but
 * is stable/deterministic for equal content across entities.
 */
@QuarkusTest
class EntityAuditServiceTest {

  @Inject
  EntityAuditService audit;

  private record FakeAuditable(String entityType, Long entityId, UUID gid, Integer region,
                               Map<String, Object> snap) implements Auditable {
    @Override public UUID auditGlobalId() { return gid; }
    @Override public Integer auditOriginRegion() { return region; }
    @Override public Map<String, Object> auditSnapshot() { return snap; }
  }

  private EntityRevision record(Auditable auditable, RevisionOp op, Principal actor) throws Throwable {
    return VertxContextSupport.subscribeAndAwait(() -> Panache.withTransaction(() -> audit.record(auditable, op, actor)));
  }

  private List<EntityRevision> revisions(Long entityId) throws Throwable {
    return VertxContextSupport.subscribeAndAwait(() -> Panache.withSession(() ->
        EntityRevision.<EntityRevision>find("entityType = ?1 and entityId = ?2 order by revision", "Post", entityId).list()));
  }

  @Test
  void recordsMonotonicRevisionsWithFederationFieldsAndActor() throws Throwable {
    long entityId = System.nanoTime();
    UUID gid = UUID.randomUUID();
    Principal actor = new Principal(1001L, null, AccountRole.USER, Set.of(), null, null);

    EntityRevision r1 = record(new FakeAuditable("Post", entityId, gid, 3,
        Map.of("caption", "first", "deleted", false)), RevisionOp.INSERT, actor);
    EntityRevision r2 = record(new FakeAuditable("Post", entityId, gid, 3,
        Map.of("caption", "edited", "deleted", false)), RevisionOp.UPDATE, actor);
    EntityRevision r3 = record(new FakeAuditable("Post", entityId, gid, 3,
        Map.of("caption", "edited", "deleted", true)), RevisionOp.DELETE, actor);

    assertEquals(1, r1.revision);
    assertEquals(RevisionOp.INSERT, r1.op);
    assertEquals(gid, r1.globalId);
    assertEquals(3, r1.originRegion);
    assertEquals(PrincipalKind.USER, r1.actorKind);
    assertEquals(1001L, r1.actorId);
    assertNotNull(r1.contentHash);
    assertNotNull(r1.snapshot);

    assertEquals(2, r2.revision);
    assertNotEquals(r1.contentHash, r2.contentHash); // caption changed

    assertEquals(3, r3.revision);
    assertNotEquals(r2.contentHash, r3.contentHash); // deleted changed

    assertEquals(3, revisions(entityId).size());
  }

  @Test
  void equalContentHashesIdenticallyAndNullActorIsUnattributed() throws Throwable {
    Map<String, Object> content = Map.of("caption", "x", "deleted", false);
    EntityRevision a = record(new FakeAuditable("Post", System.nanoTime(), UUID.randomUUID(), 1, content),
        RevisionOp.INSERT, null);
    EntityRevision b = record(new FakeAuditable("Post", System.nanoTime(), UUID.randomUUID(), 1, content),
        RevisionOp.INSERT, null);

    assertEquals(a.contentHash, b.contentHash); // canonical + deterministic
    assertNull(a.actorKind); // system/anonymous change
    assertNull(a.actorId);
  }
}
