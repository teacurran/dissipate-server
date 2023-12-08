package app.dissipate.data.models;

import jakarta.persistence.Entity;

import java.time.Instant;

@Entity
public class Url extends DefaultPanacheEntityWithTimestamps {

    public String url;

    public String domain;

    public Instant lastCrawledAt;

    String body;

}
