package app.dissipate.data.models;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/** Unit coverage for Post's federation identity + audit snapshot, and the Snowflake region decode. */
class PostAuditTest {

  @Test
  void regionOfDecodesTheSnowflakeRegionBits() {
    long idInRegion5 = 5L << 15; // region occupies bits 15-19
    assertEquals(5, SnowflakeIdGenerator.regionOf(idInRegion5));
    assertEquals(0, SnowflakeIdGenerator.regionOf(1L));
  }

  @Test
  void federateAssignsAStableGlobalIdAndOriginRegion() {
    Post post = new Post();
    post.id = 5L << 15; // region 5

    post.federate();

    assertNotNull(post.globalId);
    assertEquals(5, post.originRegion);
    assertEquals(5, post.auditOriginRegion());
    assertSame(post.globalId, post.auditGlobalId());
    assertEquals(post.id, post.entityId());
    assertEquals("Post", post.entityType());

    // federate() is idempotent — the handle is assigned once and not reused.
    var handle = post.globalId;
    post.federate();
    assertSame(handle, post.globalId);
  }

  @Test
  void auditSnapshotCapturesEditableContentOnly() {
    Post post = new Post();
    post.caption = "hello";
    post.body = "world";
    post.deleted = true;

    Map<String, Object> snapshot = post.auditSnapshot();

    assertEquals("hello", snapshot.get("caption"));
    assertEquals("world", snapshot.get("body"));
    assertEquals(true, snapshot.get("deleted"));
    assertEquals("", snapshot.get("defaultReactionEmoji")); // null coalesced
  }
}
