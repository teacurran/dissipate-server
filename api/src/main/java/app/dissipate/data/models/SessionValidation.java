package app.dissipate.data.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "session_validations")
public class SessionValidation extends DefaultPanacheEntityWithTimestamps {

  public static final String ID_GENERATOR_KEY = "SessionValidation";

  @ManyToOne
  public Session session;

  public String token;

  public Instant validated;

  @ManyToOne(fetch = FetchType.EAGER)
  public AccountEmail email;

  @ManyToOne(fetch = FetchType.EAGER)
  public AccountPhone phone;

  @Override
  public Uni<SessionValidation> persist() {
    return super.persist();
  }

  @Override
  public Uni<SessionValidation> persistAndFlush() {
    return super.persistAndFlush();
  }

  public static Uni<SessionValidation> byId(String id) {
    return SessionValidation.findById(id);
  }

}
