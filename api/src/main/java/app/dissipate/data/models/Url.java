package app.dissipate.data.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "urls")
public class Url extends DefaultPanacheEntityWithTimestamps {

  public String url;

  public String domain;

  public Instant lastCrawledAt;

  @SuppressWarnings("unchecked")
  @Override
  public Uni<Url> persistAndFlush() {
    return super.persistAndFlush();
  }

}
