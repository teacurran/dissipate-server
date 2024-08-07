package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "organizations")
public class Organization extends DefaultPanacheEntityWithTimestamps {

    public String name;

    @OneToMany(mappedBy = "organization")
    List<IdentityOrganization> accounts;

}
