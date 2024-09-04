package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organizations")
public class Organization extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne
  Organization parent;

  @ManyToOne
  HierarchyLevel level;

  public String name;

  @OneToMany(mappedBy = "organization")
  List<IdentityOrganization> accounts;

  @OneToMany(mappedBy = "parent")
  List<Organization> children = new ArrayList<>();

  @OneToMany(mappedBy = "organization")
  List<OrganizationHierarchy> hierarchies = new ArrayList<>();
}
