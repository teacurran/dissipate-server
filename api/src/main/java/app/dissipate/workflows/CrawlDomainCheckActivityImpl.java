package app.dissipate.workflows;

import io.quarkus.temporal.runtime.annotations.TemporalActivity;

import static java.lang.Thread.sleep;

@TemporalActivity
public class CrawlDomainCheckActivityImpl implements CrawlDomainCheckActivity {

    @Override
    public boolean checkDomain(String domain) {
        try {
            sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return domain.equals("dissipate.app");
    }

}
