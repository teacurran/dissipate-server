package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "identity_organizations")
public class IdentityOrganization extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne
  public Identity identity;

  @ManyToOne
  public Organization organization;

  public Instant requested;

  public Instant approved;

  public Instant accepted;

  @ManyToOne
  public Identity approvedBy;
}
