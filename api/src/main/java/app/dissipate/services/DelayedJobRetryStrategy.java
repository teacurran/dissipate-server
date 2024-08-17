package app.dissipate.services;

import java.time.Duration;
import java.time.Instant;

public class DelayedJobRetryStrategy {
  private static final Duration MAX_RETRY_INTERVAL = Duration.ofDays(20);

  public static Instant calculateNextRetryInterval(int attempts) {
    // Formula: 5 seconds + N ** 4
    Duration baseInterval = Duration.ofSeconds(5);
    Duration nextInterval = baseInterval.plusSeconds((long) Math.pow(attempts, 4));
    Duration calculated = nextInterval.compareTo(MAX_RETRY_INTERVAL) > 0 ? MAX_RETRY_INTERVAL : nextInterval;
    return Instant.now().plus(calculated);
  }
}
