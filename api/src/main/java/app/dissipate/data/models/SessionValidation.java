package app.dissipate.data.models;

import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
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

  @ManyToOne
  public AccountEmail email;

  @ManyToOne
  public AccountPhone phone;

  public Uni<SessionValidation> persist() {
    return super.persist();
  }

  public Uni<SessionValidation> persistAndFlush() {
    return super.persistAndFlush();
  }

}
