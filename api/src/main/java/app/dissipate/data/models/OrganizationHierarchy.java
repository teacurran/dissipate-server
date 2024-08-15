package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "organization_hierarchies")
public class OrganizationHierarchy extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne
  public Organization organization;

  @ManyToOne
  public Hierarchy hierarchy;
}
