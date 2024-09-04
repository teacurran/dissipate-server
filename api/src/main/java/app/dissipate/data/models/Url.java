package app.dissipate.data.models;

import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "urls")
public class Url extends DefaultPanacheEntityWithTimestamps {

  public String value;

  public String domain;

  public Instant lastCrawledAt;

  @SuppressWarnings("unchecked")
  @Override
  public Uni<Url> persistAndFlush() {
    return super.persistAndFlush();
  }

}
