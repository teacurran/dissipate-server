package app.dissipate.services.audit;

import app.dissipate.data.jpa.UuidGenerator;
import app.dissipate.data.models.AuditEvent;
import app.dissipate.data.models.AuditEventType;
import app.dissipate.data.models.AuditOutcome;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;

/**
 * Writes audit events. Recording is <strong>synchronous and in-transaction</strong>: callers invoke
 * {@link #record} inside their own {@code @WithTransaction} so the audit row commits atomically with
 * the action it describes (no audit row without the action, and vice versa). A separate scheduled
 * {@link AuditPublisher} later drains rows to an external append-only sink.
 */
@ApplicationScoped
public class AuditService {

  @Inject
  UuidGenerator uuidGenerator;

  /**
   * Persist a pre-populated audit event. Stamps the id and (if unset) {@code occurredAt}.
   * Must be called within an active reactive transaction.
   */
  @WithSpan("AuditService.record")
  public Uni<AuditEvent> record(AuditEvent event) {
    event.id = uuidGenerator.generate();
    if (event.occurredAt == null) {
      event.occurredAt = Instant.now();
    }
    return event.persistAndFlush();
  }

  /** Convenience for events that need only a type and outcome. */
  public Uni<AuditEvent> record(AuditEventType type, AuditOutcome outcome) {
    AuditEvent event = new AuditEvent();
    event.eventType = type;
    event.outcome = outcome;
    return record(event);
  }
}
