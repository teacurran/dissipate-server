package app.dissipate.data.models;

import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Per-minute API usage for a single authenticated caller, recorded by a single node. Rows are
 * retained (not pruned by the limiter) so usage can be charted per client over time; each node
 * flushes its own counts, hence the {@code (principal_type, principal_id, node_id, minute)} grain.
 * {@code requests} is the raw call count and {@code cost} the method-weighted sum.
 */
@Entity
@Table(name = "api_usage_counters")
@NamedQuery(name = ApiUsageCounter.QUERY_BY_KEY,
  query = """
    FROM ApiUsageCounter c
    WHERE c.principalType = :principalType
    AND c.principalId = :principalId
    AND c.nodeId = :nodeId
    AND c.minute = :minute
    """)
public class ApiUsageCounter extends DefaultPanacheEntityWithTimestamps {

  public static final String QUERY_BY_KEY = "ApiUsageCounter.findByKey";

  @Enumerated(EnumType.STRING)
  @Column(name = "principal_type", nullable = false)
  public PrincipalKind principalType;

  @Column(name = "principal_id", nullable = false)
  public UUID principalId;

  @Column(name = "node_id", nullable = false)
  public UUID nodeId;

  /** Start of the minute bucket (UTC), truncated to the minute. */
  @Column(nullable = false)
  public Instant minute;

  /** Raw request count in this bucket. */
  @Column(nullable = false)
  public int requests;

  /** Sum of per-method costs in this bucket. */
  @Column(nullable = false)
  public long cost;

  @Override
  @SuppressWarnings("unchecked")
  public Uni<ApiUsageCounter> persistAndFlush() {
    return super.persistAndFlush();
  }

  /** This node's counter row for the given principal + minute, or null if not yet created. */
  public static Uni<ApiUsageCounter> findByKey(PrincipalKind principalType, UUID principalId, UUID nodeId, Instant minute) {
    return find("#" + QUERY_BY_KEY, Parameters.with("principalType", principalType)
        .and("principalId", principalId).and("nodeId", nodeId).and("minute", minute)).firstResult();
  }

  /** All nodes' counter rows for the given principal + minute (one per node; sum for the global total). */
  public static Uni<java.util.List<ApiUsageCounter>> findForPrincipalMinute(PrincipalKind principalType, UUID principalId, Instant minute) {
    return list("principalType = ?1 and principalId = ?2 and minute = ?3", principalType, principalId, minute);
  }
}
