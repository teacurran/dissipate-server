package app.dissipate.services;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.models.DelayedJob;
import app.dissipate.data.models.DelayedJobQueue;
import app.dissipate.data.models.Server;
import app.dissipate.data.models.SessionValidation;
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
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.Arrays;

@ApplicationScoped
public class DelayedJobService {

  private static final Logger LOGGER = Logger.getLogger(DelayedJobService.class);

  public static final String DELAYED_JOB_RUN = "delayed-job-run";

  @Inject
  Server server;

  @Inject
  EventBus bus;

  @Inject
  SnowflakeIdGenerator snowflakeIdGenerator;

  @Inject
  DelayedJobHandlers jobHandlers;

  public Uni<DelayedJob> createDelayedJob(SessionValidation sessionValidation) {
    DelayedJob delayedJob = new DelayedJob();
    delayedJob.id = snowflakeIdGenerator.generate(DelayedJob.ID_GENERATOR_KEY);
    delayedJob.actorId = sessionValidation.id;
    delayedJob.runAt = sessionValidation.created;
    delayedJob.queue = DelayedJobQueue.EMAIL_AUTH;
    delayedJob.priority = DelayedJobQueue.EMAIL_AUTH.getPriority();

    return delayedJob.persistAndFlush().onItem().invoke(dj -> {
      if (dj == null) {
        LOGGER.error("Failed to create delayed job for session: " + sessionValidation.id);
      }
      bus.publish(DELAYED_JOB_RUN, delayedJob.id);
    });
  }

  @WithSession
  @ConsumeEvent(DELAYED_JOB_RUN)
  @WithSpan("DelayedJobService.handleDelayedJobRun")
  public Uni<Void> run(String id) {
    return getDelayedJobToWorkOn(id).onItem().transformToUni(dj -> {
      if (dj == null) {
        return Uni.createFrom().voidItem();
      }

      LOGGER.info("handling delayed job with id: " + id + " and actorId: " + dj.actorId);

      if (dj.runAt.isAfter(Instant.now())) {
        LOGGER.info("delayed job is not ready to run: " + id);

        dj.lockedBy = null;
        dj.lockedAt = null;
        return dj.persistAndFlush().onItem().transformToUni(dj2 -> {
          LOGGER.info("delayed job unlocked: " + id);
          return Uni.createFrom().voidItem();
        });
      }

      DelayedJobHandler handler = jobHandlers.get(dj.queue);
      if (handler == null) {
        LOGGER.error("No handler found for queue: " + dj.queue);
        return Uni.createFrom().voidItem();
      } else {
        return handler.run(dj.actorId)
          .onItem().transformToUni(v -> {
            dj.locked = false;
            dj.lockedAt = null;
            dj.lockedBy = null;
            dj.attempts = dj.attempts + 1;
            dj.completedAt = Instant.now();
            dj.complete = true;
            dj.lastRunBy = server;
            return dj.persistAndFlush().onItem().transformToUni(dj2 -> Uni.createFrom().voidItem());
          })
          .onFailure().recoverWithUni(t -> {
            dj.lastError = String.join("\n", Arrays.stream(t.getStackTrace()).map(StackTraceElement::toString).toArray(String[]::new));
            dj.failedAt = Instant.now();
            dj.locked = false;
            dj.lockedAt = null;
            dj.lockedBy = null;
            dj.attempts = dj.attempts + 1;

            if (t instanceof DelayedJobException dje) {
              if (dje.isFatal()) {
                dj.complete = true;
                dj.completedAt = Instant.now();
                dj.completedWithFailure = true;
              }
              dj.failureReason = dje.getMessage();
            }

            if (!dj.completedWithFailure) {
              dj.runAt = DelayedJobRetryStrategy.calculateNextRetryInterval(dj.attempts);
            }

            dj.lastRunBy = server;
            return dj.persistAndFlush().onItem().transformToUni(dj2 -> Uni.createFrom().voidItem());
          });
      }
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

  @Transactional
  @WithSpan("DelayedJobService.getDelayedJobToWorkOn")
  public Uni<DelayedJob> getDelayedJobToWorkOn(String id) {
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
      dj.lockedBy = server;
      dj.lockedAt = Instant.now();

      return dj.persistAndFlush();
    });
  }

}
