package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "identity_avatars")
public class IdentityAvatar extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  Identity identity;

  @ManyToOne
  Asset asset;

  public String name;
}
