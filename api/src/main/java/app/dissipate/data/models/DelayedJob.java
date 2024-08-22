package app.dissipate.data.models;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "delayed_jobs", indexes = {
  @Index(name = "ix_delayed_jobs_queue_run_at", columnList = "queue,runAt,complete,locked"),
})
@NamedQuery(name = DelayedJob.QUERY_FIND_READY_TO_RUN,
  query = """
    FROM DelayedJob
    WHERE runAt <= :now
    AND complete = false
    AND locked = false
    ORDER BY priority DESC, runAt ASC
    """)
public class DelayedJob extends DefaultPanacheEntityWithTimestamps {

  public static final String ID_GENERATOR_KEY = "DelayedJob";

  public static final String QUERY_FIND_READY_TO_RUN = "DelayedJob.findReadyToRun";

  public Integer priority = 0;

  public Integer attempts = 0;

  public DelayedJobQueue queue;

  @Column(columnDefinition = "VARCHAR(16)", length = 16)
  public String actorId;

  @Column(columnDefinition = "TEXT")
  public String lastError;

  public Instant runAt;

  public boolean locked = false;

  public Instant lockedAt;

  public Instant failedAt;

  public boolean complete = false;

  public Instant completedAt;

  public boolean completedWithFailure = false;

  public String failureReason;

  @ManyToOne
  public Server lockedBy;

  @ManyToOne
  public Server lastRunBy;

  public static Uni<DelayedJob> byId(String id) {
    return DelayedJob.findById(id);
  }

  public static Uni<DelayedJob> createDelayedJob(String actorId,
                                                 DelayedJobQueue queue,
                                                 Instant runAt,
                                                 SnowflakeIdGenerator snowflakeIdGenerator) {
    DelayedJob delayedJob = new DelayedJob();
    delayedJob.id = snowflakeIdGenerator.generate(DelayedJob.ID_GENERATOR_KEY);
    delayedJob.actorId = actorId;
    delayedJob.runAt = runAt;
    delayedJob.queue = queue;
    delayedJob.priority = queue.getPriority();

    return delayedJob.persistAndFlush();
  }

  @Override
  public Uni<DelayedJob> persistAndFlush() {
    return super.persistAndFlush();
  }

  public static Uni<List<DelayedJob>> findReadyToRun() {
    PanacheQuery<PanacheEntityBase> query = find("#" + DelayedJob.QUERY_FIND_READY_TO_RUN, Parameters.with("now", Instant.now()));
    query.range(0, 100);
    return query.list();
  }

}
