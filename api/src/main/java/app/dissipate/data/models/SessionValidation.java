package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import java.time.Instant;

@Entity
public class SessionValidation extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne
  public Session session;

  public String token;

  public Instant validated;

  @ManyToOne
  public AccountEmail email;

  @ManyToOne
  public AccountPhone phone;
}
