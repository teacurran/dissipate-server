package app.dissipate.services.jobs;

import app.dissipate.data.models.DelayedJobQueue;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import java.util.Map;

@ApplicationScoped
public class DelayedJobHandlerProducer {

  @Inject
  EmailAuthJobHandler emailAuthJobHandler;

  @Inject
  UrlCrawlJobHandler urlCrawlJobHandler;

  @Inject
  EtlLocationJobHandler etlLocationJobHandler;

  @Produces
  public DelayedJobHandlers getJobHandlers() {
    return new DelayedJobHandlers(
      Map.of(
        DelayedJobQueue.EMAIL_AUTH, emailAuthJobHandler,
        DelayedJobQueue.URL_CRAWL, urlCrawlJobHandler,
        DelayedJobQueue.ETL_LOCATION, etlLocationJobHandler
      ));
  }
}
