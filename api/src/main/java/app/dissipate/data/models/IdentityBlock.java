package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "identity_blocks")
public class IdentityBlock extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  public Identity identity;

  @ManyToOne
  public Identity identity2;

  public IdentityBlockType type;

  public Instant expires;

  public boolean isMutual;

}
