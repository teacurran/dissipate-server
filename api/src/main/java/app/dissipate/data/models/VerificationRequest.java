package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class VerificationRequest extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  public Account account;
}
