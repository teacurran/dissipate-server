package app.dissipate.services.jobs;

import io.smallrye.mutiny.Uni;

public interface DelayedJobHandler {
  Uni<Void> run(String actorId);
}
