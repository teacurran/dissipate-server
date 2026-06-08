package app.dissipate.services.audit;

import app.dissipate.data.models.AuditEvent;
import io.smallrye.mutiny.Uni;

import java.util.List;

/**
 * Drains audit events to an external, durable, append-only sink (SIEM, log pipeline, object store,
 * etc.). Pluggable: provide an alternative {@code @ApplicationScoped} implementation to override the
 * default {@link StructuredLogAuditPublisher} (which is a {@code @DefaultBean}).
 *
 * <p>Implementations must be idempotent-tolerant: a publish may be retried (e.g. node restart
 * between emit and {@code published_at} commit), so the sink should accept occasional duplicates
 * keyed by {@link AuditEvent#id}.
 */
public interface AuditPublisher {

  /**
   * Emit a batch to the external sink. Completing successfully signals the events are durably
   * accepted and may be marked published; a failure leaves them for the next drain cycle.
   */
  Uni<Void> publish(List<AuditEvent> events);
}
