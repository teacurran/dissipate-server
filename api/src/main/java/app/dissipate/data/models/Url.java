package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "urls")
public class Url extends DefaultPanacheEntityWithTimestamps {

    public String url;

    public String domain;

    public Instant lastCrawledAt;

    String body;

}
