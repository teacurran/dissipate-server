package app.dissipate.data.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "delayed_jobs", indexes = {
  @Index(name = "ix_delayed_jobs_queue_run_at", columnList = "queue,runAt"),
})
public class DelayedJob extends DefaultPanacheEntityWithTimestamps {

  public static final String ID_GENERATOR_KEY = "DelayedJob";

  public Integer priority = 0;

  public Integer attempts = 0;

  public DelayedJobQueue queue;

  @Column(columnDefinition = "CHAR(13)", length = 13)
  public String actorId;

  public String lastError;

  public Instant runAt;

  public Instant lockedAt;

  public Instant failedAt;

  @ManyToOne
  public Server lockedBy;

  @ManyToOne
  public Server lastRunBy;

}
