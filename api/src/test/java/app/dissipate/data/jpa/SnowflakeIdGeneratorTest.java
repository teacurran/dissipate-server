package app.dissipate.data.jpa;

import app.dissipate.data.models.Server;
import app.dissipate.services.ServerInstance;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SnowflakeIdGeneratorTest {
  private SnowflakeIdGenerator snowflakeIdGenerator;
  private ServerInstance serverInstance;

  @BeforeEach
  void setup() {
    serverInstance = mock(ServerInstance.class);
    Server mockServer = new Server();
    mockServer.instanceNumber = 1;
    when(serverInstance.getCurrentServer()).thenReturn(mockServer);
    snowflakeIdGenerator = new SnowflakeIdGenerator(serverInstance, 0);
  }

  @Test
  void testGenerateUniqueIds() throws HibernateException {
    long id1 = snowflakeIdGenerator.generate("test");
    long id2 = snowflakeIdGenerator.generate("test");
    assertNotEquals(id1, id2);
  }

  @Test
  void testGenerateIdsInQuickSuccession() throws HibernateException {
    long id1 = snowflakeIdGenerator.generate("test");
    long id2 = snowflakeIdGenerator.generate("test");
    long id3 = snowflakeIdGenerator.generate("test");
    assertNotEquals(id1, id2);
    assertNotEquals(id2, id3);
    assertNotEquals(id1, id3);
  }

  @Test
  void testReturnedIdIsPositive() {
    long id = snowflakeIdGenerator.generate("test");
    assertTrue(id > 0, "Snowflake ids must be non-negative (sign bit 0)");
  }

  @Test
  void testSystemClockMovesBackward() {
    SnowflakeIdGenerator.Snowflake snowflake = new SnowflakeIdGenerator.Snowflake(1, 1);
    snowflake.nextId();
    // Simulate system clock moving backward by advancing the recorded last timestamp.
    snowflake.setLastTimestamp(snowflake.lastTimestamp.get() + 1000);
    assertThrows(IllegalStateException.class, snowflake::nextId);
  }

  @Test
  void testBitLayoutEncodeDecodeRoundTrip() {
    SnowflakeIdGenerator.Snowflake snowflake = new SnowflakeIdGenerator.Snowflake(257, 7);
    long id = snowflake.nextId();
    SnowflakeIdGenerator.Decoded decoded = SnowflakeIdGenerator.decode(id);
    assertEquals(7L, decoded.region());
    assertEquals(257L, decoded.instance());
    assertTrue(decoded.sequence() <= SnowflakeIdGenerator.Snowflake.MAX_SEQUENCE);
    long now = System.currentTimeMillis();
    assertTrue(Math.abs(now - decoded.timestampMs()) < 5_000,
      "decoded timestamp should be within 5s of now (got " + decoded.timestampMs() + " vs " + now + ")");
  }

  @Test
  void testMonotonicOrderingAcrossTimestamps() throws InterruptedException {
    SnowflakeIdGenerator.Snowflake snowflake = new SnowflakeIdGenerator.Snowflake(1, 0);
    long first = snowflake.nextId();
    Thread.sleep(2);
    long second = snowflake.nextId();
    assertTrue(second > first, "Later id should sort higher: first=" + first + " second=" + second);
  }

  @Test
  void testSequenceWrapWithinAMillisecond() {
    // Pin time to a single millisecond by overriding timestamp(). The generator must
    // produce MAX_SEQUENCE+1 unique IDs within one millisecond and then, when the
    // sequence wraps to zero, wait for the clock to advance instead of colliding.
    final long fixed = SnowflakeIdGenerator.CUSTOM_EPOCH_MS + 1_000_000L;
    final long[] clock = {fixed};
    SnowflakeIdGenerator.Snowflake snowflake = new SnowflakeIdGenerator.Snowflake(1, 0) {
      @Override
      long timestamp() {
        return clock[0];
      }
    };

    int maxSeqPlusOne = (int) SnowflakeIdGenerator.Snowflake.MAX_SEQUENCE + 1;
    Set<Long> seen = new HashSet<>();
    for (int i = 0; i < maxSeqPlusOne; i++) {
      long id = snowflake.nextId();
      assertTrue(seen.add(id), "Duplicate id at iteration " + i + ": " + id);
      SnowflakeIdGenerator.Decoded decoded = SnowflakeIdGenerator.decode(id);
      assertEquals(fixed - SnowflakeIdGenerator.CUSTOM_EPOCH_MS,
        decoded.timestampMs() - SnowflakeIdGenerator.CUSTOM_EPOCH_MS,
        "Timestamp should remain pinned at fixed");
      assertEquals(i, decoded.sequence(), "Sequence should be the iteration counter");
    }

    // The (MAX_SEQUENCE+2)-th call would normally wrap to sequence=0 and block in
    // waitForTimeToChange until time moves. Advance the clock from another thread
    // so the call returns instead of deadlocking.
    Thread advancer = new Thread(() -> {
      try {
        Thread.sleep(50);
      } catch (InterruptedException ignored) {
        Thread.currentThread().interrupt();
      }
      clock[0] = fixed + 1;
    });
    advancer.start();
    long afterWait = snowflake.nextId();
    assertTrue(seen.add(afterWait), "Post-wait id must be unique");
    SnowflakeIdGenerator.Decoded post = SnowflakeIdGenerator.decode(afterWait);
    assertEquals(fixed + 1, post.timestampMs(),
      "After wrap, the generator must have advanced to the next millisecond");
    assertEquals(0L, post.sequence(), "Sequence resets at new millisecond");
  }

  @Test
  void testCustomEpochBoundary() {
    final long[] clock = {SnowflakeIdGenerator.CUSTOM_EPOCH_MS};
    SnowflakeIdGenerator.Snowflake snowflake = new SnowflakeIdGenerator.Snowflake(0, 0) {
      @Override
      long timestamp() {
        return clock[0];
      }
    };
    long idAtEpoch = snowflake.nextId();
    assertEquals(0L, SnowflakeIdGenerator.decode(idAtEpoch).timestampMs() - SnowflakeIdGenerator.CUSTOM_EPOCH_MS);

    clock[0] = SnowflakeIdGenerator.CUSTOM_EPOCH_MS + 1;
    long idAtEpochPlus1 = snowflake.nextId();
    assertEquals(1L, SnowflakeIdGenerator.decode(idAtEpochPlus1).timestampMs() - SnowflakeIdGenerator.CUSTOM_EPOCH_MS);
  }

  @Test
  void testRegionOutOfRangeRejected() {
    assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator.Snowflake(0, -1));
    assertThrows(IllegalArgumentException.class,
      () -> new SnowflakeIdGenerator.Snowflake(0, (int) SnowflakeIdGenerator.Snowflake.MAX_REGION_ID + 1));
  }

  @Test
  void testInstanceOutOfRangeRejected() {
    assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator.Snowflake(-1, 0));
    assertThrows(IllegalArgumentException.class,
      () -> new SnowflakeIdGenerator.Snowflake((int) SnowflakeIdGenerator.Snowflake.MAX_INSTANCE_ID + 1, 0));
  }

  @Test
  void testCustomEpochConstantMatches2026() {
    // CUSTOM_EPOCH_MS == 1767225600000L == 2026-01-01T00:00:00Z
    assertEquals(1767225600000L, SnowflakeIdGenerator.CUSTOM_EPOCH_MS);
  }

  @Test
  void testGenerateNeverReturnsNegative() {
    for (int i = 0; i < 100; i++) {
      long id = snowflakeIdGenerator.generate("test-" + (i % 4));
      assertFalse(id < 0, "Snowflake id must not be negative: " + id);
    }
    assertNotNull(snowflakeIdGenerator);
  }
}
