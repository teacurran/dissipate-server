package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "assets")
public class Asset extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  public Identity creator;

  @ManyToOne
  public Identity owner;
}
