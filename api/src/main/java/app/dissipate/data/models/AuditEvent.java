package app.dissipate.data.models;

import app.dissipate.utils.SnowflakeBase36Deserializer;
import app.dissipate.utils.SnowflakeBase36Serializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Operational audit log row. Written synchronously in the same transaction as the action it
 * records (see {@code AuditService}); a scheduled {@code AuditPublisher} later drains rows where
 * {@link #publishedAt} is null to an external append-only sink.
 *
 * <p>Snowflake-id reference columns ({@link #actorAccountId}, {@link #actorIdentityId},
 * {@link #targetId}) are loosely coupled BIGINTs rather than FKs so audit rows survive deletion of
 * the entities they describe.
 */
@Entity
@Table(name = "audit_events")
@NamedQuery(name = AuditEvent.QUERY_UNPUBLISHED,
  query = """
    FROM AuditEvent
    WHERE publishedAt IS NULL
    ORDER BY occurredAt ASC
    """)
public class AuditEvent extends DefaultPanacheEntityWithTimestamps {

  public static final String ID_GENERATOR_KEY = "AuditEvent";
  public static final String QUERY_UNPUBLISHED = "AuditEvent.findUnpublished";

  @Column(nullable = false)
  public Instant occurredAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public AuditEventType eventType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public AuditOutcome outcome;

  @Column(columnDefinition = "BIGINT")
  @JsonSerialize(using = SnowflakeBase36Serializer.class)
  @JsonDeserialize(using = SnowflakeBase36Deserializer.class)
  public Long actorAccountId;

  @Column(columnDefinition = "BIGINT")
  @JsonSerialize(using = SnowflakeBase36Serializer.class)
  @JsonDeserialize(using = SnowflakeBase36Deserializer.class)
  public Long actorIdentityId;

  /** Session under which the action occurred (sessions use UUID ids). */
  public UUID sessionId;

  /** Logical type of the entity acted on, e.g. {@code Identity}, {@code Session}. */
  public String targetType;

  @Column(columnDefinition = "BIGINT")
  @JsonSerialize(using = SnowflakeBase36Serializer.class)
  @JsonDeserialize(using = SnowflakeBase36Deserializer.class)
  public Long targetId;

  public String clientIp;

  @Column(columnDefinition = "TEXT")
  public String userAgent;

  /** Short machine/human reason, e.g. an error code on a FAILURE outcome. */
  public String reason;

  /** Free-form structured context as a JSON document (jsonb). */
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  public String metadata;

  /** Set when the AuditPublisher has emitted this row to the external sink. */
  public Instant publishedAt;

  /** Set when a retention job has cold-stored/pruned this row externally. */
  public Instant archivedAt;

  @Override
  @SuppressWarnings("unchecked")
  public Uni<AuditEvent> persistAndFlush() {
    return super.persistAndFlush();
  }

  /** Oldest-first batch of rows not yet emitted by the publisher. */
  public static Uni<List<AuditEvent>> findUnpublished(int limit) {
    return AuditEvent.<AuditEvent>find("#" + QUERY_UNPUBLISHED)
      .range(0, Math.max(0, limit - 1))
      .list();
  }
}
