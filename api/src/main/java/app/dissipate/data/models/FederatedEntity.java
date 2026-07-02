package app.dissipate.data.models;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.util.UUID;

/**
 * Base for entities that are published/replicated across regions and (later) federated out over
 * ActivityPub / AT Protocol. On top of the internal Snowflake BIGINT id it carries a stable,
 * protocol-neutral global handle ({@link #globalId}) — the identity all external and cross-region
 * references use, never the BIGINT — and the {@link #originRegion} that authored it. Every federated
 * entity is {@link Auditable}; its revisions double as the cross-region reconciliation log.
 */
@MappedSuperclass
public abstract class FederatedEntity extends DefaultPanacheEntityWithTimestamps implements Auditable {

  /** Stable, protocol-neutral global handle; assigned once at creation and never reused. */
  @Column(name = "global_id", nullable = false, unique = true, updatable = false)
  public UUID globalId;

  /** The region that authored this entity (its authority), decoded from the Snowflake id. */
  @Column(name = "origin_region", nullable = false, updatable = false)
  public int originRegion;

  /** Assign the federation identity from the already-set Snowflake id. Call once at creation. */
  public void federate() {
    if (globalId == null) {
      globalId = UUID.randomUUID();
    }
    if (id != null) {
      originRegion = SnowflakeIdGenerator.regionOf(id);
    }
  }

  @Override
  public Long entityId() {
    return id;
  }

  @Override
  public UUID auditGlobalId() {
    return globalId;
  }

  @Override
  public Integer auditOriginRegion() {
    return originRegion;
  }
}
