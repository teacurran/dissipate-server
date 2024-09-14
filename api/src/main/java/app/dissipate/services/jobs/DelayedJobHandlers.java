package app.dissipate.services.jobs;

import app.dissipate.data.models.DelayedJobQueue;

import java.util.Map;

public class DelayedJobHandlers {

  private final Map<DelayedJobQueue, DelayedJobHandler> handlers;

  public DelayedJobHandlers(Map<DelayedJobQueue, DelayedJobHandler> handlers) {
    this.handlers = handlers;
  }

  public DelayedJobHandler get(DelayedJobQueue queue) {
    return handlers.get(queue);
  }
}
