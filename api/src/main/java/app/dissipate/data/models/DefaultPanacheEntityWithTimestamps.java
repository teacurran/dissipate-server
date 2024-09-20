package app.dissipate.data.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@MappedSuperclass
public abstract class DefaultPanacheEntityWithTimestamps extends PanacheEntityBase {

  @Id
  @Column(columnDefinition = "VARCHAR(16)", length = 16)
  public String id;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  public Instant created;

  @UpdateTimestamp
  public Instant updated;

  public String toString() {
    String var10000 = this.getClass().getSimpleName();
    return var10000 + "<" + this.id + ">";
  }
}
