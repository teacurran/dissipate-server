package app.dissipate.models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Account extends DefaultPanacheEntity {
    public String email;
    public String phone;
    public AccountStatus status;

    @OneToMany(
            mappedBy = "account",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    public List<Handle> handles = new ArrayList<>();

    public void setEmail(String email){
        this.email = email.toLowerCase();
    }
}

