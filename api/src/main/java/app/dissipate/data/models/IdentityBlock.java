package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "identity_blocks")
public class IdentityBlock extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  public Identity identity;

  @ManyToOne
  public Identity identity2;

  public boolean isMutual;

}
