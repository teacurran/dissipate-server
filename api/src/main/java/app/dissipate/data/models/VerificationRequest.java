package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "verification_requests")
public class VerificationRequest extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  public Account account;

  @ManyToOne
  public Order order;
}
