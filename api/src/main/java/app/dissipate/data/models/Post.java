package app.dissipate.data.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Entity
public class Post extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    public Identity identity;

    @ManyToOne(fetch = FetchType.LAZY)
    public Channel channel;

}
