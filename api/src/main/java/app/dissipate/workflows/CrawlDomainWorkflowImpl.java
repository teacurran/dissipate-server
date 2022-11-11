package app.dissipate.workflows;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.temporal.runtime.annotations.TemporalActivityStub;
import io.quarkus.temporal.runtime.annotations.TemporalWorkflow;

import javax.inject.Inject;

@TemporalWorkflow(queue = "crawl-domain", name = "crawl-domain")
public class CrawlDomainWorkflowImpl implements CrawlDomainWorkflow {

    @TemporalActivityStub
    CrawlDomainCheckActivity crawlDomainCheckActivity;

    @Inject
    Tracer tracer;

    @WithSpan("crawl-domain")
    @Override
    public void crawlDomain(@SpanAttribute(value = "domain") String domain) {
        tracer.spanBuilder("crawl-domain").startSpan().setAttribute("domain", domain);

        if (crawlDomainCheckActivity.checkDomain(domain)) {
            System.out.println("Domain " + domain + " is valid");
        } else {
            System.out.println("Domain " + domain + " is invalid");
        }
    }
}
