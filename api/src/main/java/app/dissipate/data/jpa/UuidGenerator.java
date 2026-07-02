package app.dissipate.data.jpa;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

/**
 * Mints UUIDv7 (RFC 9562) primary keys in-app, mirroring the assignment ergonomics of the
 * retired Snowflake generator ({@code entity.id = uuidGenerator.generate()}).
 *
 * <p>UUIDv7 is time-ordered (48-bit Unix-ms prefix), giving B-tree insert locality like the old
 * Snowflake ids, but leaks no region/node/rate and needs no instance lease to stay collision-free.
 * The monotonic variant keeps ids strictly increasing within a single millisecond on this node.
 * Generation is a plain value computation, so it is safe under Hibernate Reactive.
 *
 * <p>Injectable (rather than a static util) so tests can mock deterministic ids.
 */
@ApplicationScoped
public class UuidGenerator {

  /** A fresh, time-ordered, monotonic UUIDv7. */
  public UUID generate() {
    return UuidCreator.getTimeOrderedEpochPlus1();
  }
}
