package app.dissipate.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Entity
public class Handle extends PanacheEntity {
    public String handle;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;
}
