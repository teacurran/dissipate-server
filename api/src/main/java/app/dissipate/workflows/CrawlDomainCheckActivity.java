package app.dissipate.workflows;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface CrawlDomainCheckActivity {
    boolean checkDomain(String domain);
}
