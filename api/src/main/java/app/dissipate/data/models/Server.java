package app.dissipate.data.models;

import app.dissipate.data.models.dto.MaxIntDto;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "servers")
@NamedQuery(name = Server.QUERY_FIND_FIRST_UNUSED_SERVER,
  query = "from Server where isShutdown=true")
@NamedQuery(name = Server.QUERY_MARK_ABANDONED_SERVERS_AS_SHUTDOWN,
  query = "update Server set isShutdown=true, status=:status where seen < :seen AND isShutdown=false")
public class Server extends PanacheEntityBase {

  public static final String QUERY_FIND_FIRST_UNUSED_SERVER = "Server.findFirstUnusedServer";

  public static final String QUERY_MARK_ABANDONED_SERVERS_AS_SHUTDOWN = "Server.markAbandonedServersAsShutdown";

  private static final Duration ABANDONED_SERVER_DURATION = Duration.ofDays(1);

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @CreationTimestamp
  @Column(name = "created",
    nullable = false, updatable = false)
  public Instant created;

  @UpdateTimestamp
  @Column(name = "updated")
  public Instant updated;

  @Column(name = "instance_number")
  public int instanceNumber;

  public Instant seen;

  public Instant launched;

  @Column(name = "is_shutdown",
    nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
  public boolean isShutdown;

  public ServerStatus status;

  public String hostname;

  public Integer port;

  public String token;

  public static Uni<MaxIntDto> findMaxInstanceId() {
    return find("select coalesce(max(instanceNumber), 0) as maxValue from Server").project(MaxIntDto.class).firstResult();
  }

  public static Uni<Integer> markAbandonedServersAsShutdown(ZoneOffset zoneOffset) {
    Instant seenThreshold = LocalDateTime.now().toInstant(zoneOffset).minus(ABANDONED_SERVER_DURATION);

    return update("#" + QUERY_MARK_ABANDONED_SERVERS_AS_SHUTDOWN,
      Parameters.with("status", ServerStatus.SHUTDOWN)
        .and("seen", seenThreshold));
  }

  public static Uni<Server> findFirstUnusedServer() {
    return find("isShutdown=true").firstResult();
  }

  public static Uni<Server> byId(Long id) {
    return findById(id);
  }
}
