package app.dissipate.data.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;

import java.time.Instant;

@MappedSuperclass
public abstract class DefaultPanacheEntityWithTimestamps extends PanacheEntityBase {

    @Id
    @Column(columnDefinition = "CHAR(13)", length = 13)
    public String id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    public Instant updatedAt;

    public String toString() {
        String var10000 = this.getClass().getSimpleName();
        return var10000 + "<" + this.id + ">";
    }

}
