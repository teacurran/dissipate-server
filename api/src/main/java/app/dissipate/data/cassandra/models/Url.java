package app.dissipate.data.cassandra.models;

import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import java.time.LocalDate;

@Entity
public class Url {

    @PartitionKey
    String url;

    String domain;

    LocalDate dateCreated;

    LocalDate dateLastCrawled;

    String body;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public LocalDate getDateLastCrawled() {
        return dateLastCrawled;
    }

    public void setDateLastCrawled(LocalDate dateLastCrawled) {
        this.dateLastCrawled = dateLastCrawled;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
