package app.dissipate.services;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.models.DelayedJob;
import app.dissipate.data.models.DelayedJobQueue;
import app.dissipate.data.models.Server;
import app.dissipate.data.models.SessionValidation;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.Instant;

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
  public Uni<Void> handleDelayedJobCreated(String id) {
    return getDelayedJobToWorkOn(id).onItem().transformToUni(dj -> {
      if (dj == null) {
        return Uni.createFrom().voidItem();
      }

      LOGGER.info("handling delayed job with id: " + id + " and actorId: " + dj.actorId);

      return Uni.createFrom().voidItem();
    });
  }

  @Transactional
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

      return dj.persistAndFlush();
    });
  }

}
