package app.dissipate.services;

import app.dissipate.data.jpa.UuidGenerator;
import app.dissipate.data.models.DelayedJob;
import app.dissipate.data.models.DelayedJobQueue;
import app.dissipate.data.models.Server;
import app.dissipate.data.models.SessionValidation;
import app.dissipate.data.models.Url;
import app.dissipate.exceptions.DelayedJobException;
import app.dissipate.services.jobs.DelayedJobHandler;
import app.dissipate.services.jobs.DelayedJobHandlers;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

@ApplicationScoped
public class DelayedJobService {

  private static final Logger LOGGER = Logger.getLogger(DelayedJobService.class);

  public static final String DELAYED_JOB_RUN = "delayed-job-run";

  @Inject
  @Named("currentServer")
  Server currentServer;

  @Inject
  EventBus bus;

  @Inject
  UuidGenerator uuidGenerator;

  @Inject
  DelayedJobHandlers jobHandlers;

  /**
   * Maximum send attempts before the job is permanently failed.
   *
   * <p>Applied to every queue (currently EMAIL_AUTH, PHONE_AUTH, URL_CRAWL,
   * ETL_LOCATION). Set via {@code dissipate.delayed-job.max-attempts}; default
   * 3 matches the brief for OTP delivery.</p>
   */
  @ConfigProperty(name = "dissipate.delayed-job.max-attempts", defaultValue = "3")
  int maxAttempts;

  public Uni<DelayedJob> createDelayedJob(UUID actorId, DelayedJobQueue queue, Instant runAt) {
    return DelayedJob.createDelayedJob(actorId, queue, runAt, uuidGenerator)
      .onItem().invoke(dj -> {
        if (dj == null) {
          LOGGER.error("Failed to create delayed job for actor: " + actorId);
        }
        bus.publish(DELAYED_JOB_RUN, dj.id);
      });
  }

  public Uni<DelayedJob> createDelayedJob(SessionValidation sessionValidation) {
    // Choose the right queue based on which side-effect the validation needs.
    // Phone numbers route through PHONE_AUTH (SMS handler is TODO), everything
    // else (including the no-side-effect case) falls through to EMAIL_AUTH so
    // the EmailAuthJobHandler can no-op safely if neither email nor phone is
    // set on the row.
    DelayedJobQueue queue = sessionValidation.phone != null
      ? DelayedJobQueue.PHONE_AUTH
      : DelayedJobQueue.EMAIL_AUTH;
    return createDelayedJob(sessionValidation.id, queue, sessionValidation.created);
  }

  public Uni<DelayedJob> createDelayedJob(Url url) {
    return createDelayedJob(url.id, DelayedJobQueue.URL_CRAWL, Instant.now());
  }

  @WithSession
  @ConsumeEvent(DELAYED_JOB_RUN)
  @WithSpan("DelayedJobService.handleDelayedJobRun")
  public Uni<Void> run(UUID id) {
    return getDelayedJobToWorkOn(id)
      .onItem()
      .ifNotNull()
      .transformToUni(dj -> {

        LOGGER.info("handling delayed job with id: " + id + " and actorId: " + dj.actorId);

        if (dj.runAt.isAfter(Instant.now())) {
          LOGGER.info("delayed job is not ready to run: " + id);

          return dj.unlock().onItem().transformToUni(v -> Uni.createFrom().voidItem());
        }

        DelayedJobHandler handler = jobHandlers.get(dj.queue);
        if (handler == null) {
          LOGGER.error("No handler found for queue: " + dj.queue + " (job " + dj.id + ")");
          // Mark the job permanently failed; without this it would keep being
          // picked up by the scheduler every 30s.
          dj.complete = true;
          dj.completedAt = Instant.now();
          dj.completedWithFailure = true;
          dj.failureReason = "No handler registered for queue " + dj.queue;
          dj.lastRunBy = currentServer;
          return dj.unlock().onItem().transformToUni(v -> Uni.createFrom().voidItem());
        }
        return handler.run(dj.actorId)
          .onItem().transformToUni(v -> {
            dj.attempts = dj.attempts + 1;
            dj.completedAt = Instant.now();
            dj.complete = true;
            dj.lastRunBy = currentServer;
            return dj.unlock().onItem().transformToUni(dj2 -> Uni.createFrom().voidItem());
          })
          .onFailure().recoverWithUni(t -> {
            dj.lastError = String.join("\n", Arrays.stream(t.getStackTrace()).map(StackTraceElement::toString).toArray(String[]::new));
            dj.failedAt = Instant.now();
            dj.attempts = dj.attempts + 1;

            if (t instanceof DelayedJobException dje) {
              if (dje.isFatal()) {
                dj.complete = true;
                dj.completedAt = Instant.now();
                dj.completedWithFailure = true;
              }
              dj.failureReason = dje.getMessage();
            } else {
              dj.failureReason = t.getMessage();
            }

            // Stop polling once max attempts reached; otherwise the row would
            // loop forever and starve the scheduler.
            if (!dj.completedWithFailure && dj.attempts >= maxAttempts) {
              LOGGER.warnv("delayed job {0} exceeded max attempts ({1}); marking permanently failed",
                dj.id, maxAttempts);
              dj.complete = true;
              dj.completedAt = Instant.now();
              dj.completedWithFailure = true;
            }

            if (!dj.completedWithFailure) {
              dj.runAt = DelayedJobRetryStrategy.calculateNextRetryInterval(dj.attempts);
            }

            dj.lastRunBy = currentServer;
            return dj.unlock().onItem().transformToUni(dj2 -> Uni.createFrom().voidItem());
          });

      });
  }

  @Scheduled(every = "30s")
  @WithSession
  @WithSpan("DelayedJobService.run")
  Uni<Void> run() {
    return DelayedJob.findReadyToRun().onItem().transformToUni(djs -> {
      if (djs.isEmpty()) {
        return Uni.createFrom().voidItem();
      }

      LOGGER.info("found " + djs.size() + " delayed jobs to run");

      return Uni.combine().all().unis(
        djs.stream()
          .map(dj -> bus.request(DELAYED_JOB_RUN, dj.id).replaceWith(Uni.createFrom().voidItem()))
          .toArray(Uni[]::new)
      ).discardItems();
    });
  }

  @WithSpan("DelayedJobService.getDelayedJobToWorkOn")
  public Uni<DelayedJob> getDelayedJobToWorkOn(UUID id) {
    return DelayedJob.byId(id).onItem().transformToUni(dj -> {
      if (dj == null) {
        LOGGER.error("DelayedJob not found: " + id);
        return Uni.createFrom().nullItem();
      }

      if (dj.locked) {
        LOGGER.error("DelayedJob already locked: " + id);
        return Uni.createFrom().nullItem();
      }

      dj.locked = true;
      dj.lockedBy = currentServer;
      dj.lockedAt = Instant.now();

      return dj.persistAndFlush();
    });
  }

}
