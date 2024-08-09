package app.dissipate.interceptors;

import app.dissipate.data.models.Server;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;

import static app.dissipate.utils.DateUtil.OTEL_DATE_TIME_FORMATTER;

@ApplicationScoped
@RegisterForReflection
public class ApplicationSpanProcessor implements SpanProcessor {

//  This results in an infinite loop because the ServerInstance class is injected
//  @Inject
//  Server server;

//  final Server server;

//  public ApplicationSpanProcessor(Server server) {
//    this.server = server;
//  }


  @Override
  public void onStart(Context context, ReadWriteSpan span) {
//    span.setAttribute("server.instance", server.instanceNumber);
//    span.setAttribute("server.started", OTEL_DATE_TIME_FORMATTER.format(server.launched));

    span.setAttribute("server.instance", "56");
    span.setAttribute("server.started", "2024-01-01T00:00:00Z");

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
