package app.dissipate.interceptors;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.quarkus.grpc.GlobalInterceptor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.Prioritized;
import jakarta.inject.Inject;

/**
 * Final stage of the auth pipeline: records a Micrometer timer for every gRPC call, tagged by
 * fully-qualified method and the terminal status code. Highest priority so it runs outermost and
 * still times calls that a downstream interceptor (e.g. authn) rejects. Exposed via the Prometheus
 * registry at {@code /q/metrics}.
 *
 * <p>Ordering is via {@link Prioritized} (higher = invoked first / outermost) because Quarkus gRPC
 * orders interceptors by that interface, not the {@code @Priority} annotation.
 */
@GlobalInterceptor
@ApplicationScoped
public class GrpcMetricsInterceptor implements ServerInterceptor, Prioritized {

  /** Pipeline order (higher runs first/outermost): metrics &gt; authn &gt; validation. */
  public static final int PRIORITY = 200;

  /** Timer name for server-side gRPC call latency + counts. */
  public static final String CALLS_TIMER = "grpc.server.calls";

  @Inject
  MeterRegistry registry;

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @SuppressWarnings("java:S119")
  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {

    Timer.Sample sample = Timer.start(registry);
    String method = call.getMethodDescriptor().getFullMethodName();

    ServerCall<ReqT, RespT> timed =
        new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
          @Override
          public void close(Status status, Metadata trailers) {
            sample.stop(registry.timer(CALLS_TIMER, "method", method, "code", status.getCode().name()));
            super.close(status, trailers);
          }
        };

    return next.startCall(timed, metadata);
  }
}
