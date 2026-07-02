package app.dissipate.data.jpa;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Plain JUnit coverage for {@link UuidGenerator} (UUIDv7, time-ordered monotonic). */
class UuidGeneratorTest {

  private final UuidGenerator generator = new UuidGenerator();

  @Test
  void generatesNonNullDistinctVersion7() {
    UUID a = generator.generate();
    UUID b = generator.generate();

    assertNotNull(a);
    assertNotNull(b);
    assertTrue(!a.equals(b), "successive generate() calls must be distinct");
    assertEquals(7, a.version(), "must be a UUIDv7");
    assertEquals(7, b.version(), "must be a UUIDv7");
  }

  @Test
  void successiveIdsAreMonotonicallyIncreasing() {
    UUID prev = generator.generate();
    for (int i = 0; i < 1000; i++) {
      UUID next = generator.generate();
      assertTrue(compareUnsigned(prev, next) < 0,
          "UUIDv7 ids must be strictly time-ordered/monotonic within a run: "
              + prev + " should sort before " + next);
      prev = next;
    }
  }

  /** Lexicographic (unsigned, big-endian) comparison of two UUIDs. */
  private static int compareUnsigned(UUID a, UUID b) {
    int hi = Long.compareUnsigned(a.getMostSignificantBits(), b.getMostSignificantBits());
    if (hi != 0) {
      return hi;
    }
    return Long.compareUnsigned(a.getLeastSignificantBits(), b.getLeastSignificantBits());
  }
}
