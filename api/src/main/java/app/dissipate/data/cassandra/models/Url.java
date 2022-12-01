package app.dissipate.data.cassandra.models;

import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import java.time.Instant;

@Entity
public class Url {

    public Url() {
    }

    public Url(String url) {
        this.url = url;
    }

    @PartitionKey
    String url;

    String domain;

    Instant dateCreated;

    Instant dateLastCrawled;

    String body;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Instant dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Instant getDateLastCrawled() {
        return dateLastCrawled;
    }

    public void setDateLastCrawled(Instant dateLastCrawled) {
        this.dateLastCrawled = dateLastCrawled;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
