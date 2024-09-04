package app.dissipate.data.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "permissions")
public class Permission extends DefaultPanacheEntityWithTimestamps {
  public String key;
  public String name;

  @OneToMany(
    mappedBy = "permission",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  public List<IdentityPermission> identities = new ArrayList<>();
}
