package app.dissipate.data.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Entity
public class Channel extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    public Identity identity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    public Account account;

    public String name;
}
