package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum DelayedJobQueue {
  EMAIL_AUTH(100),
  PHONE_AUTH(100),
  ETL_LOCATION(100),
  EMAIL_MARKETING(0),
  URL_CRAWL(50),
  LICENSE_PROVISION(50);

  private final int priority;

  DelayedJobQueue(int priority) {
    this.priority = priority;
  }

  public int getPriority() {
    return priority;
  }
}
