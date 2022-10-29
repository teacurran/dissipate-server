package app.dissipate.workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface CrawlDomainWorkflow {

    @WorkflowMethod
    void crawlDomain(String domain);
}
