package app.dissipate.data.models;

import app.dissipate.utils.SnowflakeBase36Deserializer;
import app.dissipate.utils.SnowflakeBase36Serializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@MappedSuperclass
public abstract class DefaultPanacheEntityWithTimestamps extends PanacheEntityBase {

  /**
   * Snowflake-encoded BIGINT primary key. JSON-serialized as base-36 for compactness
   * and JavaScript-precision-safety; database storage is {@code BIGINT}.
   */
  @Id
  @Column(columnDefinition = "BIGINT")
  @JsonSerialize(using = SnowflakeBase36Serializer.class)
  @JsonDeserialize(using = SnowflakeBase36Deserializer.class)
  public Long id;

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
    String rendered = this.id == null ? "null" : Long.toString(this.id, 36);
    return name + "<" + rendered + ">";
  }
}
