package app.dissipate.services.jobs;

import app.dissipate.services.EtlLocation;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class EtlLocationJobHandler implements DelayedJobHandler {

  @Inject
  EtlLocation etlLocation;

  @Override
  @WithSpan("EtlLocationJobHandler.run")
  public Uni<Void> run(UUID actorId) {
    return etlLocation.loadWorldLocations();
  }
}
