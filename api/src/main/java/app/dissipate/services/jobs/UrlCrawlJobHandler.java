package app.dissipate.services.jobs;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UrlCrawlJobHandler implements DelayedJobHandler {

  @Override
  @WithSpan("EmailAuthJobHandler.run")
  public Uni<Void> run(String actorId) {
    return Uni.createFrom().voidItem();
  }
}
