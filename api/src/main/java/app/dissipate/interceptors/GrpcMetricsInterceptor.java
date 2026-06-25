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
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Final stage of the auth pipeline: records a Micrometer timer for every gRPC call, tagged by
 * fully-qualified method and the terminal status code. Highest priority so it runs outermost and
 * still times calls that a downstream interceptor (e.g. authn) rejects. Exposed via the Prometheus
 * registry at {@code /q/metrics}.
 */
@GlobalInterceptor
@ApplicationScoped
@Priority(200)
public class GrpcMetricsInterceptor implements ServerInterceptor {

  /** Timer name for server-side gRPC call latency + counts. */
  public static final String CALLS_TIMER = "grpc.server.calls";

  @Inject
  MeterRegistry registry;

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
