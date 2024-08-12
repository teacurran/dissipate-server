package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import java.time.Instant;

@Entity
public class IdentityFollow extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne
  public Identity identity;

  @ManyToOne
  public Identity identity2;

  public Instant requested;

  public Instant approved;

  public boolean isMutual;

}
