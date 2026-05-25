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
@NamedQuery(name = SessionValidation.QUERY_BY_SID,
  query = """
    FROM SessionValidation sv
    JOIN FETCH sv.session
    LEFT JOIN FETCH sv.email
    LEFT JOIN FETCH sv.phone
    LEFT JOIN FETCH sv.session.account
    WHERE sv.session.id = :sid
    """)
@NamedQuery(name = SessionValidation.QUERY_BY_SID_TOKEN,
  query = """
    FROM SessionValidation sv
    JOIN FETCH sv.session
    LEFT JOIN FETCH sv.email
    LEFT JOIN FETCH sv.phone
    WHERE sv.session.id = :sid
    AND sv.token = :token
    """)
@NamedQuery(name = SessionValidation.QUERY_BY_ID,
  query = """
    FROM SessionValidation sv
    JOIN FETCH sv.session
    LEFT JOIN FETCH sv.session.account
    LEFT JOIN FETCH sv.email
    LEFT JOIN FETCH sv.phone
    WHERE sv.id = :id
    """)
public class SessionValidation extends DefaultPanacheEntityWithTimestamps {

  public static final String ID_GENERATOR_KEY = "SessionValidation";

  public static final String QUERY_BY_SID = "SessionValidation.findBySid";
  public static final String QUERY_BY_SID_TOKEN = "SessionValidation.findBySidToken";
  public static final String QUERY_BY_ID = "SessionValidation.findById";

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

  public static Uni<SessionValidation> byId(Long id) {
    // Eagerly fetch related rows so callers running outside a Hibernate session
    // (e.g. background job handlers) can dereference session/account/email/phone.
    return find("#" + QUERY_BY_ID, Parameters.with("id", id)).firstResult();
  }

  public static Uni<SessionValidation> findBySid(String sid) {
    return find("#" + SessionValidation.QUERY_BY_SID_TOKEN,
      Parameters.with("sid", UUID.fromString(sid))).firstResult();
  }

  public static Uni<SessionValidation> findBySidToken(String sid, String token) {
    return find("#" + SessionValidation.QUERY_BY_SID_TOKEN,
      Parameters.with("sid", UUID.fromString(sid)).and("token", token.toUpperCase())).firstResult();
  }

}
