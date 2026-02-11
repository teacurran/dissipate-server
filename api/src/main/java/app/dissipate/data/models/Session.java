package app.dissipate.data.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sessions")
@NamedQuery(name = Session.QUERY_BY_SID,
  query = """
    SELECT DISTINCT s
    FROM Session s
    LEFT JOIN FETCH s.account a
    LEFT JOIN FETCH s.identity i
    WHERE s.id = :sid
    """)
@NamedQuery(name = Session.QUERY_BY_SID_VALIDATED,
  query = """
    SELECT DISTINCT s
    FROM Session s
    JOIN FETCH s.validations sv
    LEFT JOIN FETCH s.identity i
    WHERE s.id = :sid
    AND s.ended IS NULL
    AND sv.validated IS NOT NULL
    """)
@NamedQuery(name = Session.QUERY_FIND_CONNECTED_BY_IDENTITY_IDS,
  query = """
    SELECT s
    FROM Session s
    JOIN FETCH s.connectedServer
    WHERE s.identity.id IN :identityIds
    AND s.connectedServer IS NOT NULL
    AND s.ended IS NULL
    """)
public class Session extends PanacheEntityBase {

  public static final String QUERY_BY_SID = "Session.findBySid";
  public static final String QUERY_BY_SID_VALIDATED = "Session.findBySidValidated";
  public static final String QUERY_FIND_CONNECTED_BY_IDENTITY_IDS = "Session.findConnectedByIdentityIds";

  @Id
  @GeneratedValue
  public UUID id;

  @ManyToOne
  public Account account;

  @ManyToOne
  public Identity identity;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  public Instant created;

  @UpdateTimestamp
  public Instant updated;

  public Instant ended;

  public boolean loggedIn;

  public String clientIp;

  @ManyToOne
  public Server connectedServer;

  @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
  public List<SessionValidation> validations = new ArrayList<>();

  @Override
  @SuppressWarnings("unchecked")
  public Uni<Session> persist() {
    return super.persist();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Uni<Session> persistAndFlush() {
    return super.persistAndFlush();
  }

  public static Uni<Session> findBySid(UUID sid) {
    return find("#" + Session.QUERY_BY_SID, Parameters.with("sid", sid)).firstResult();
  }

  public static Uni<Session> findBySidValidated(String sid) {
    return find("#" + Session.QUERY_BY_SID_VALIDATED, Parameters.with("sid", UUID.fromString(sid))).firstResult();
  }

  public static Uni<List<Session>> findConnectedByIdentityIds(List<String> identityIds) {
    return find("#" + Session.QUERY_FIND_CONNECTED_BY_IDENTITY_IDS,
      Parameters.with("identityIds", identityIds)).list();
  }
}
