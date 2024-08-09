package app.dissipate.data.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sessions")
public class Session  extends PanacheEntityBase {

  @Id
  @GeneratedValue
  public UUID id;

  @ManyToOne
  public Account account;

  @ManyToOne
  public Identity identity;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  public Instant created;

  @UpdateTimestamp
  public Instant updated;

  public Instant ended;

  public Uni<Session> persist() {
    return super.persist();
  }

  public Uni<Session> persistAndFlush() {
    return super.persistAndFlush();
  }

}
