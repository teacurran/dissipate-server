package app.dissipate.workflows;

import io.quarkus.temporal.runtime.annotations.TemporalActivity;

@TemporalActivity
public class CrawlDomainCheckActivityImpl implements CrawlDomainCheckActivity {

    @Override
    public boolean checkDomain(String domain) {
        return domain.equals("dissipate.app");
    }

}
