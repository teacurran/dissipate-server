package app.dissipate.services;

import app.dissipate.data.models.Url;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UrlService {

  @Inject
  DelayedJobService delayedJobService;

  @WithSession
  @WithTransaction
  public Uni<Url> addUrl(final String value) {
    Url url = new Url();
    url.value = value;
    return url.persistAndFlush().onItem().transformToUni(u -> {
      return delayedJobService.createDelayedJob(url).onItem().transform(dj -> {
        return url;
      });
    });
  }
}
