package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "verification_request_notes")
public class VerificationRequestNote extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  public VerificationRequest verificationRequest;

  @ManyToOne
  public Identity identity;

  public String note;
}
