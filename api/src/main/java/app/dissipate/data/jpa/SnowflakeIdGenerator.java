package app.dissipate.data.jpa;

import app.dissipate.services.ServerInstance;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.HibernateException;

import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Snowflake ID generator producing 63-bit positive longs with a custom epoch.
 *
 * <p>Bit layout (sign bit is always 0):
 * <pre>
 * bit  63   : sign (always 0)
 * bits 20-62: timestamp     (43 bits, ms since CUSTOM_EPOCH_MS)  -> ~278 years
 * bits 15-19: region        ( 5 bits)                            -> 32 regions
 * bits  5-14: instance      (10 bits)                            -> 1024 per region
 * bits  0- 4: sequence      ( 5 bits)                            -> 32 IDs/ms/node
 * </pre>
 *
 * <p>The custom epoch ({@value #CUSTOM_EPOCH_MS}) corresponds to
 * {@code 2026-01-01T00:00:00Z}, giving roughly 278 years of headroom before the
 * timestamp portion overflows the 43-bit field.
 */
@ApplicationScoped
public class SnowflakeIdGenerator {

  /**
   * Custom epoch: {@code 2026-01-01T00:00:00Z} expressed as milliseconds since the Unix epoch.
   * Timestamps in generated IDs are measured as {@code System time - CUSTOM_EPOCH_MS}.
   */
  public static final long CUSTOM_EPOCH_MS = 1767225600000L;

  private final ServerInstance serverInstance;
  private final int region;

  private final HashMap<String, Snowflake> snowflakes = new HashMap<>();

  SnowflakeIdGenerator(ServerInstance serverInstance,
                       @ConfigProperty(name = "dissipate.region", defaultValue = "0") int region) {
    this.serverInstance = serverInstance;
    this.region = region;
  }

  public long generate(String idName) throws HibernateException {
    Snowflake snowflake = snowflakes.get(idName);
    if (snowflake == null) {
      snowflake = new Snowflake(serverInstance.getCurrentServer().instanceNumber, region);
      snowflakes.put(idName, snowflake);
    }
    return snowflake.nextId();
  }

  static class Snowflake {
    static final int SEQUENCE_BITS = 5;   // 0-31
    static final int INSTANCE_BITS = 10;  // 0-1023
    static final int REGION_BITS = 5;     // 0-31
    static final int TIMESTAMP_BITS = 43; // ~278 years from CUSTOM_EPOCH_MS

    static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;
    static final long MAX_INSTANCE_ID = (1L << INSTANCE_BITS) - 1;
    static final long MAX_REGION_ID = (1L << REGION_BITS) - 1;
    static final long MAX_TIMESTAMP = (1L << TIMESTAMP_BITS) - 1;

    static final int INSTANCE_SHIFT = SEQUENCE_BITS;                       // 5
    static final int REGION_SHIFT = SEQUENCE_BITS + INSTANCE_BITS;         // 15
    static final int TIMESTAMP_SHIFT =
      SEQUENCE_BITS + INSTANCE_BITS + REGION_BITS;                         // 20

    private final ReentrantLock lock = new ReentrantLock();
    private final long regionNumber;
    private final long instanceNumber;
    private final AtomicLong sequence = new AtomicLong(0L);

    final AtomicLong lastTimestamp = new AtomicLong(-1L);

    public Snowflake(int instanceNumber, int regionNumber) {
      if (regionNumber < 0 || regionNumber > MAX_REGION_ID) {
        throw new IllegalArgumentException(
          String.format("Region %s out of range %d - %d", regionNumber, 0, MAX_REGION_ID));
      }
      if (instanceNumber < 0 || instanceNumber > MAX_INSTANCE_ID) {
        throw new IllegalArgumentException(
          String.format("Instance %s out of range %d - %d", instanceNumber, 0, MAX_INSTANCE_ID));
      }
      this.regionNumber = regionNumber;
      this.instanceNumber = instanceNumber;
    }

    public long nextId() {
      lock.lock();
      try {
        long currentTimestamp = timestamp();

        if (currentTimestamp < lastTimestamp.get()) {
          throw new IllegalStateException("Invalid System Clock!");
        }

        if (currentTimestamp == lastTimestamp.get()) {
          long next = (sequence.get() + 1) & MAX_SEQUENCE;
          sequence.set(next);
          if (next == 0) {
            currentTimestamp = waitForTimeToChange(currentTimestamp);
          }
        } else {
          sequence.set(0);
        }

        lastTimestamp.set(currentTimestamp);

        long epochOffset = currentTimestamp - CUSTOM_EPOCH_MS;
        if (epochOffset < 0 || epochOffset > MAX_TIMESTAMP) {
          throw new IllegalStateException(
            "Timestamp out of range for Snowflake encoding: " + epochOffset);
        }

        return (epochOffset << TIMESTAMP_SHIFT)
          | (regionNumber << REGION_SHIFT)
          | (instanceNumber << INSTANCE_SHIFT)
          | sequence.get();
      } finally {
        lock.unlock();
      }
    }

    long timestamp() {
      return Instant.now().toEpochMilli();
    }

    private long waitForTimeToChange(long currentTimestamp) {
      while (currentTimestamp == lastTimestamp.get()) {
        currentTimestamp = timestamp();
      }
      return currentTimestamp;
    }

    void setLastTimestamp(long timestamp) {
      this.lastTimestamp.set(timestamp);
    }
  }

  /** Decoded view of a Snowflake-encoded {@code long} id. */
  public record Decoded(long timestampMs, long region, long instance, long sequence) {
  }

  /**
   * Decode a Snowflake-encoded {@code long} into its constituent fields.
   *
   * @param id encoded id
   * @return decoded fields; {@code timestampMs} is wall-clock millis since Unix epoch
   *         (i.e. the stored offset plus {@link #CUSTOM_EPOCH_MS}).
   */
  public static Decoded decode(long id) {
    long sequence = id & Snowflake.MAX_SEQUENCE;
    long instance = (id >>> Snowflake.INSTANCE_SHIFT) & Snowflake.MAX_INSTANCE_ID;
    long region = (id >>> Snowflake.REGION_SHIFT) & Snowflake.MAX_REGION_ID;
    long timestampOffset = (id >>> Snowflake.TIMESTAMP_SHIFT) & Snowflake.MAX_TIMESTAMP;
    return new Decoded(timestampOffset + CUSTOM_EPOCH_MS, region, instance, sequence);
  }
}
