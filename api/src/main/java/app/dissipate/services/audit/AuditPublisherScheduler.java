package app.dissipate.services.audit;

import app.dissipate.data.models.AuditEvent;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Instant;

/**
 * Periodically drains not-yet-published {@link AuditEvent}s to the {@link AuditPublisher} and stamps
 * {@code published_at}. Mirrors the {@code delayed_jobs} scheduler pattern.
 *
 * <p>Marking published is committed in the same transaction as the read; the publish itself happens
 * before the stamp, so a crash between emit and commit re-publishes the batch next cycle (at-least-
 * once — the {@code AuditPublisher} contract tolerates duplicates). Cross-node de-duplication of the
 * drain is out of scope for the default structured-log publisher.
 */
@ApplicationScoped
public class AuditPublisherScheduler {

  private static final Logger LOGGER = Logger.getLogger(AuditPublisherScheduler.class);

  @Inject
  AuditPublisher auditPublisher;

  @ConfigProperty(name = "dissipate.audit.publish-batch-size", defaultValue = "200")
  int batchSize;

  @Scheduled(every = "{dissipate.audit.publish-interval}",
    concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
  @WithTransaction
  @WithSpan("AuditPublisherScheduler.drain")
  Uni<Void> drain() {
    return AuditEvent.findUnpublished(batchSize).onItem().transformToUni(events -> {
      if (events.isEmpty()) {
        return Uni.createFrom().voidItem();
      }

      LOGGER.debugv("draining {0} audit event(s) to publisher", events.size());

      return auditPublisher.publish(events).onItem().transformToUni(v -> {
        Instant now = Instant.now();
        events.forEach(e -> e.publishedAt = now);
        // Entities are managed in this transaction; flush the publishedAt updates before commit.
        return Panache.getSession().onItem().transformToUni(session -> session.flush()).replaceWithVoid();
      });
    });
  }
}
