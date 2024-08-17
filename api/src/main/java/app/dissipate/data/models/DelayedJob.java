package app.dissipate.data.models;

import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "delayed_jobs", indexes = {
  @Index(name = "ix_delayed_jobs_queue_run_at", columnList = "queue,runAt,completedAt"),
})
public class DelayedJob extends DefaultPanacheEntityWithTimestamps {

  public static final String ID_GENERATOR_KEY = "DelayedJob";

  public Integer priority = 0;

  public Integer attempts = 0;

  public DelayedJobQueue queue;

  @Column(columnDefinition = "CHAR(13)", length = 13)
  public String actorId;

  @Column(columnDefinition = "TEXT")
  public String lastError;

  public Instant runAt;

  public Instant lockedAt;

  public Instant failedAt;

  public boolean complete = false;

  public Instant completedAt;

  @ManyToOne
  public Server lockedBy;

  @ManyToOne
  public Server lastRunBy;

  public static Uni<DelayedJob> byId(String id) {
    return DelayedJob.findById(id);
  }

  @Override
  public Uni<DelayedJob> persistAndFlush() {
    return super.persistAndFlush();
  }

}
