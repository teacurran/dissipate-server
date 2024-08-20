package app.dissipate.data.jpa;

import app.dissipate.constants.ApplicationConstants;
import app.dissipate.services.ServerInstance;
import org.hibernate.HibernateException;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@ApplicationScoped
public class SnowflakeIdGenerator {

    private final ServerInstance serverInstance;

    SnowflakeIdGenerator(ServerInstance serverInstance) {
        this.serverInstance = serverInstance;
    }

    HashMap<String, Snowflake> snowflakes = new HashMap<>();

    public String generate(String idName) throws HibernateException {
        Snowflake snowflake;
        if (snowflakes.containsKey(idName)) {
            snowflake = snowflakes.get(idName);
        } else {
            snowflake = new Snowflake(serverInstance.getServer().instanceNumber, 0, 0);
            snowflakes.put(idName, snowflake);
        }
        long newId = snowflake.nextId();
        return Long.toString(newId, Character.MAX_RADIX);
    }

    static class Snowflake {
      private static final int REGION_BITS = 8;
      private static final int INSTANCE_BITS = 10;
      private static final int SEQUENCE_BITS = 12;
      private static final int ADDITIONAL_ID_BITS = 8;
      // informational only: 45 bits left = (2^{45}) milliseconds ≈ 1,099.5 years
      // private static final int TIMESTAMP_BITS = 45;

      private static final long MAX_REGION_ID = (1L << REGION_BITS) - 1;
      private static final long MAX_INSTANCE_ID = (1L << INSTANCE_BITS) - 1;
      private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;
      private static final long MAX_ADDITIONAL_ID = (1L << ADDITIONAL_ID_BITS) - 1;

        private final ReentrantLock lock = new ReentrantLock();
      private final long regionNumber;
      private final long instanceNumber;
      private final long additionalId;
        private final AtomicLong lastTimestamp = new AtomicLong(-1L);
        private final AtomicLong sequence = new AtomicLong(0L);

        public Snowflake(int instanceNumber, int regionNumber, int additionalId) {
          if (regionNumber < 0 || regionNumber > MAX_REGION_ID) {
            throw new IllegalArgumentException(String.format("Region %s out of range %d - %d", regionNumber, 0, MAX_REGION_ID));
          }
          if (instanceNumber < 0 || instanceNumber > MAX_INSTANCE_ID) {
            throw new IllegalArgumentException(String.format("Instance %s out of range %d - %d", instanceNumber, 0, MAX_INSTANCE_ID));
          }
          if (additionalId < 0 || additionalId > MAX_ADDITIONAL_ID) {
            throw new IllegalArgumentException(String.format("Additional ID %s out of range %d - %d", additionalId, 0, MAX_ADDITIONAL_ID));
          }
          this.regionNumber = regionNumber;
          this.instanceNumber = instanceNumber;
          this.additionalId = additionalId;
        }

        public long nextId() {
            lock.lock();
            try {
                var currentTimestamp = timestamp();

                if(currentTimestamp < lastTimestamp.get()) {
                    throw new IllegalStateException("Invalid System Clock!");
                }

                if (currentTimestamp == lastTimestamp.get()) {
                    sequence.set((sequence.get() + 1) & MAX_SEQUENCE);
                    if(sequence.get() == 0) {
                        currentTimestamp = waitForTimeToChange(currentTimestamp);
                    }
                } else {
                    sequence.set(0);
                }

                lastTimestamp.set(currentTimestamp);

              return ((currentTimestamp << (REGION_BITS + INSTANCE_BITS + SEQUENCE_BITS + ADDITIONAL_ID_BITS)) >>> 1)
                | (regionNumber << (INSTANCE_BITS + SEQUENCE_BITS + ADDITIONAL_ID_BITS))
                | (instanceNumber << (SEQUENCE_BITS + ADDITIONAL_ID_BITS))
                | (additionalId << SEQUENCE_BITS)
                | sequence.get();
            } finally {
                lock.unlock();
            }
        }

        private long timestamp() {
            return Instant.now().toEpochMilli();
        }

        private long waitForTimeToChange(long currentTimestamp) {
            while (currentTimestamp == lastTimestamp.get()) {
                currentTimestamp = timestamp();
            }
            return currentTimestamp;
        }
    }
}
