package app.dissipate.data.cassandra.models;

import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import java.time.Instant;

@Entity
public class Domain {

    public Domain() {
    }

    public Domain(String domain) {
        this.domain = domain;
    }

    @PartitionKey
    String domain;

    Instant dateCreated;

    Instant dateLastCrawled;

    String serverType;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Instant getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Instant dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Instant getDateLastCrawled() {
        return dateLastCrawled;
    }

    public void setDateLastCrawled(Instant dateLastCrawled) {
        this.dateLastCrawled = dateLastCrawled;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }
}
