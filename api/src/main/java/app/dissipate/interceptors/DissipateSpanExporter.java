package app.dissipate.interceptors;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;

// no-op span processor
// I was looking into this as a way of filtering out ApiExceptions
// but a filter is probably the right place for that
@Unremovable
@ApplicationScoped
public class DissipateSpanExporter implements SpanProcessor {

  @Override
  public void onStart(Context parentContext, ReadWriteSpan span) {
  }

  @Override
  public boolean isStartRequired() {
    return false;
  }

  @Override
  public void onEnd(ReadableSpan span) {
  }

  @Override
  public boolean isEndRequired() {
    return false;
  }

  @Override
  public CompletableResultCode shutdown() {
    return SpanProcessor.super.shutdown();
  }

  @Override
  public CompletableResultCode forceFlush() {
    return SpanProcessor.super.forceFlush();
  }

  @Override
  public void close() {
    SpanProcessor.super.close();
  }
}
