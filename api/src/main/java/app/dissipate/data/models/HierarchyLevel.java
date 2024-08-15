package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "hierarchy_levels")
public class HierarchyLevel extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  Hierarchy hierarchy;

  String name;
}
