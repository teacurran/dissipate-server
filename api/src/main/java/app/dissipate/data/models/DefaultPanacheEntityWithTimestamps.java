package app.dissipate.data.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
public abstract class DefaultPanacheEntityWithTimestamps extends PanacheEntityBase {

  /**
   * UUIDv7 (RFC 9562) primary key, assigned in-app at creation via
   * {@link app.dissipate.data.jpa.UuidGenerator}. Time-ordered for index locality and
   * protocol-neutral for federation; stored as native Postgres {@code uuid}.
   */
  @Id
  @Column(columnDefinition = "uuid")
  public UUID id;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  public Instant created;

  @UpdateTimestamp
  public Instant updated;

  @Version
  public Long version;

  @Override
  public String toString() {
    String name = this.getClass().getSimpleName();
    String rendered = this.id == null ? "null" : this.id.toString();
    return name + "<" + rendered + ">";
  }
}
