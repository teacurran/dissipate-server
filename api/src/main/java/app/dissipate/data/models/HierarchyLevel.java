package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class HierarchyLevel extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  Hierarchy hierarchy;

  String name;
}
