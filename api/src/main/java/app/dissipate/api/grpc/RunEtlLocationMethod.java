package app.dissipate.api.grpc;

import app.dissipate.data.models.DelayedJobQueue;
import app.dissipate.grpc.RunEtlLocationRequest;
import app.dissipate.grpc.RunEtlLocationResponse;
import app.dissipate.services.DelayedJobService;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;

@ApplicationScoped
public class RunEtlLocationMethod {

  @Inject
  DelayedJobService delayedJobService;

  @WithSpan("RunEtlLocationMethod.run")
  public Uni<RunEtlLocationResponse> run(RunEtlLocationRequest request) {
    return delayedJobService.createDelayedJob(null, DelayedJobQueue.ETL_LOCATION, Instant.now())
      .onItem().transform(dj -> RunEtlLocationResponse.newBuilder().build());
  }
}
