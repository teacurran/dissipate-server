package app.dissipate.data.models;

import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "session_validations")
@NamedQuery(name = SessionValidation.QUERY_BY_SID_TOKEN,
  query = """
    FROM SessionValidation sv
    JOIN FETCH sv.session
    LEFT JOIN FETCH sv.email
    LEFT JOIN FETCH sv.phone
    WHERE sv.session.id = :sid
    AND sv.token = :token
    """)
public class SessionValidation extends DefaultPanacheEntityWithTimestamps {

  public static final String ID_GENERATOR_KEY = "SessionValidation";

  public static final String QUERY_BY_SID_TOKEN = "SessionValidation.findBySidToken";

  @ManyToOne
  public Session session;

  public String token;

  public Instant validated;

  @ManyToOne(fetch = FetchType.EAGER)
  public AccountEmail email;

  @ManyToOne(fetch = FetchType.EAGER)
  public AccountPhone phone;

  @Override
  @SuppressWarnings("unchecked")
  public Uni<SessionValidation> persist() {
    return super.persist();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Uni<SessionValidation> persistAndFlush() {
    return super.persistAndFlush();
  }

  public static Uni<SessionValidation> byId(String id) {
    return findById(id);
  }

  public static Uni<SessionValidation> findBySidToken(String sid, String token) {
    return find("#" + SessionValidation.QUERY_BY_SID_TOKEN,
      Parameters.with("sid", UUID.fromString(sid)).and("token", token.toUpperCase())).firstResult();
  }

}
