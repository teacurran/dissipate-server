package app.dissipate;

import app.dissipate.workflows.CrawlDomainWorkflow;
import io.quarkus.temporal.runtime.builder.WorkflowBuilder;
import io.temporal.client.WorkflowClient;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {

    @Inject
    WorkflowBuilder workflowBuilder;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        CrawlDomainWorkflow cdw = workflowBuilder.build(CrawlDomainWorkflow.class, "crawl-domain");
        WorkflowClient.execute(cdw::crawlDomain, "https://mastodon.social/");

        return "Hello from dissipate!";
    }
}