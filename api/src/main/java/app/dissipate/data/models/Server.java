package app.dissipate.data.models;

import app.dissipate.data.jpa.converters.RegionConverter;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * A node in the fabric: its registry row and the address other nodes route to for real-time
 * (chat) delivery. Location is no longer encoded in ids, so this row carries the node's
 * {@link #region} and network address, and a {@code sessions.connected_server_id} presence link
 * tells us which node a user is currently connected to.
 */
@Entity
@Table(name = "servers")
@NamedQuery(name = Server.QUERY_MARK_ABANDONED_SERVERS_AS_SHUTDOWN,
  query = "update Server set isShutdown=true, status=:status where seen < :seen AND isShutdown=false")
public class Server extends PanacheEntityBase {

  public static final String QUERY_MARK_ABANDONED_SERVERS_AS_SHUTDOWN = "Server.markAbandonedServersAsShutdown";

  private static final Duration ABANDONED_SERVER_DURATION = Duration.ofDays(1);

  /** UUIDv7 primary key, assigned in-app at registration (no instance lease needed). */
  @Id
  @Column(columnDefinition = "uuid")
  public UUID id;

  @CreationTimestamp
  @Column(name = "created",
    nullable = false, updatable = false)
  public Instant created;

  @UpdateTimestamp
  @Column(name = "updated")
  public Instant updated;

  /** The region this node runs in — the target region for cross-region routing. */
  @Convert(converter = RegionConverter.class)
  @Column(name = "region", length = 16)
  public Region region;

  public Instant seen;

  public Instant launched;

  @Column(name = "is_shutdown",
    nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
  public boolean isShutdown;

  public ServerStatus status;

  public String hostname;

  public Integer port;

  public String token;

  public static Uni<Integer> markAbandonedServersAsShutdown(ZoneOffset zoneOffset) {
    Instant seenThreshold = LocalDateTime.now().toInstant(zoneOffset).minus(ABANDONED_SERVER_DURATION);

    return update("#" + QUERY_MARK_ABANDONED_SERVERS_AS_SHUTDOWN,
      Parameters.with("status", ServerStatus.SHUTDOWN)
        .and("seen", seenThreshold));
  }

  public static Uni<Server> byId(UUID id) {
    return findById(id);
  }
}
