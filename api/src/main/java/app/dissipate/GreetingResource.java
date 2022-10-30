package app.dissipate;

import app.dissipate.workflows.CrawlDomainWorkflow;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.quarkus.temporal.runtime.builder.WorkflowBuilder;
import io.temporal.api.common.v1.WorkflowExecution;
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
        WorkflowExecution we = WorkflowClient.start(cdw::crawlDomain, "https://mastodon.social/");
        Span.current().addEvent("Started Workflow", Attributes.of(AttributeKey.stringKey("workflow-id"), we.getWorkflowId()));

        return "Hello from dissipate!";
    }
}