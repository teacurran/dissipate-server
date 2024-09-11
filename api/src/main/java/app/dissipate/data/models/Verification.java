package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "verifications")
public class Verification extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  Account account;

  public Instant expiresAt;

  public Instant approvedAt;

  @ManyToOne
  public Identity approvedBy;
}
