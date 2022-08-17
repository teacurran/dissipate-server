package app.dissipate.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Account extends PanacheEntity {
    @Column(unique = true)
    public String srcId;
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

