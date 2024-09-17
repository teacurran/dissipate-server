package app.dissipate.interceptors;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterForReflection
public class ApplicationSpanProcessor implements SpanProcessor {

  @Override
  public void onStart(Context context, ReadWriteSpan span) {
    // the goal here was to inject server.instanceNumber into every span
    // I can't get it working because if I inject Server, that itself creates a span and it goes into an infinite loop
  }

  @Override
  public boolean isStartRequired() {
    return true;
  }

  @Override
  public void onEnd(ReadableSpan readableSpan) {
    // do nothing
  }

  @Override
  public boolean isEndRequired() {
    return false;
  }
}
