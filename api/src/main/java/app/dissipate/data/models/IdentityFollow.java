package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "identity_follows")
public class IdentityFollow extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne(optional = false)
  public Identity identity;

  @ManyToOne
  public Identity identity2;

  public Instant requested;

  public Instant approved;

  public boolean isMutual;

}
