package app.dissipate.data.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "channels")
public class Channel extends DefaultPanacheEntityWithTimestamps {

    @ManyToOne(optional = false)
    public Identity identity;

    @ManyToOne(optional = false)
    public Account account;

    public String name;
}
