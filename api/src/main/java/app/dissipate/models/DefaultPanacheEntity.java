package app.dissipate.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class DefaultPanacheEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public DefaultPanacheEntity() {
    }

    public String toString() {
        String var10000 = this.getClass().getSimpleName();
        return var10000 + "<" + this.id + ">";
    }

}
