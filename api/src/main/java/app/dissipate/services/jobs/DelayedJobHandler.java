package app.dissipate.services.jobs;

import io.smallrye.mutiny.Uni;

import java.util.UUID;

public interface DelayedJobHandler {
  Uni<Void> run(UUID actorId);
}
