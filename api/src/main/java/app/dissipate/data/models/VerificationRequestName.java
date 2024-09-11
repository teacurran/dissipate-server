package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "verification_request_names")
public class VerificationRequestName extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  public VerificationRequest verificationRequest;

  public String name;
}
