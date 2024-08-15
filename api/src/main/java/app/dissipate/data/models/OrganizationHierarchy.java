package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class OrganizationHierarchy extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne
  public Organization organization;

  @ManyToOne
  public Hierarchy hierarchy;
}
