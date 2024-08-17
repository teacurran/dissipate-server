package app.dissipate.services;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.models.DelayedJob;
import app.dissipate.data.models.DelayedJobQueue;
import app.dissipate.data.models.Server;
import app.dissipate.data.models.SessionValidation;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
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
import java.util.List;

@ApplicationScoped
public class DelayedJobService {

  private static final Logger LOGGER = Logger.getLogger(DelayedJobService.class);

  public static final String DELAYED_JOB_CREATED = "delayed-job-created";

  @Inject
  Server server;

  @Inject
  EventBus bus;

  @Inject
  SnowflakeIdGenerator snowflakeIdGenerator;

  @Inject
  ReactiveMailer mailer;

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
      bus.publish(DELAYED_JOB_CREATED, delayedJob.id);
    });
  }

  @WithSession
  @ConsumeEvent(DELAYED_JOB_CREATED)
  @WithSpan("DelayedJobService.handleDelayedJobCreated")
  public Uni<Void> handleDelayedJobCreated(String id) {
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

      switch (dj.queue) {
        case EMAIL_AUTH:
          return handleEmailAuth(dj.actorId)
            .onItem().transformToUni(v -> {
              dj.lockedBy = null;
              dj.lockedAt = null;
              dj.attempts = dj.attempts + 1;
              dj.completedAt = Instant.now();
              dj.complete = true;
              dj.lastRunBy = server;
              return dj.persistAndFlush().onItem().transformToUni(dj2 -> {
                LOGGER.info("delayed job unlocked: " + id);
                return Uni.createFrom().voidItem();
              });
            })
            .onFailure().call(t -> {
              LOGGER.error("Error completing delayed job: " + id, t);
              dj.lastError = String.join("\n", Arrays.stream(t.getStackTrace()).map(StackTraceElement::toString).toArray(String[]::new));
              dj.failedAt = Instant.now();
              dj.attempts = dj.attempts + 1;
              dj.locked = false;
              dj.lockedBy = null;
              dj.lockedAt = null;
              dj.runAt = DelayedJobRetryStrategy.calculateNextRetryInterval(dj.attempts);
              dj.lastRunBy = server;
              return dj.persistAndFlush();
            });
        default:
          LOGGER.error("Unknown queue: " + dj.queue);
          return Uni.createFrom().voidItem();
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

      return Uni.combine().all().unis(djs.stream().map(dj -> {
        return getDelayedJobToWorkOn(dj.id).onItem().transformToUni(dj2 -> {
          if (dj2 == null) {
            return Uni.createFrom().voidItem();
          }

          return bus.request(DELAYED_JOB_CREATED, dj2.id).onItem().transformToUni(v -> {
            return Uni.createFrom().voidItem();
          });
        });
      }).toArray(Uni[]::new)).discardItems();
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

      if (dj.lockedBy != null) {
        LOGGER.error("DelayedJob already locked: " + id);
        return Uni.createFrom().nullItem();
      }

      dj.lockedAt = Instant.now();
      dj.lockedBy = server;
      dj.locked = true;

      return dj.persistAndFlush();
    });
  }

  @Transactional
  @WithSpan("DelayedJobService.handleEmailAuth")
  public Uni<Void> handleEmailAuth(String id) {
    return SessionValidation.byId(id).onItem().transformToUni(sessionValidation -> {
      if (sessionValidation == null) {
        LOGGER.error("SessionValidation not found: " + id);
        return Uni.createFrom().voidItem();
      }

      if (sessionValidation.email != null) {
        Span.current().setAttribute("email", sessionValidation.email.email);
        Mail m = new Mail();
        m.setFrom("admin@hallofjustice.net");
        m.setTo(List.of(sessionValidation.email.email));
        m.setText("Lex Luthor has been seen in Gotham City!");
        m.setSubject("WARNING: Super Villain Alert");

        LOGGER.info("handleSessionValidation(): " + sessionValidation.email.email);

        return mailer.send(m);
      } else if (sessionValidation.phone != null) {
        LOGGER.info("handleSessionValidation(): " + sessionValidation.phone.phone);
      } else {
        LOGGER.error("SessionValidation has no email or phone: " + id);
      }

      return Uni.createFrom().voidItem();
    });
  }

}
