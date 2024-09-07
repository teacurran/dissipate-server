package app.dissipate.constants;

import java.time.Duration;

public class ApplicationConstants {
  public static final long APP_EPOCH = 946684800000L;

  public static final long MAX_DB_WAIT_TIME = 5000L;
  public static final Duration MAX_DB_WAIT_DURATION = Duration.ofMillis(MAX_DB_WAIT_TIME);

  private ApplicationConstants() {
    // class cannot be instantiated
  }
}
