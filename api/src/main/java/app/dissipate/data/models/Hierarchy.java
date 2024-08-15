package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "hierarchies")
public class Hierarchy extends DefaultPanacheEntityWithTimestamps {
  public String name;
  public String description;

  @ManyToOne
  public Organization organization;
}
