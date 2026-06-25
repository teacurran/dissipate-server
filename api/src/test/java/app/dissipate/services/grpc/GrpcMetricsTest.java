package app.dissipate.services.grpc;

import app.dissipate.constants.AuthenticationConstants;
import app.dissipate.grpc.v1.GetSessionRequest;
import app.dissipate.grpc.v1.SessionService;
import app.dissipate.grpc.v1.SessionServiceGrpc;
import app.dissipate.interceptors.GrpcMetricsInterceptor;
import io.grpc.Metadata;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.grpc.GrpcClientUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the {@link GrpcMetricsInterceptor} records a per-method, per-status timer for gRPC calls
 * — both a successful authenticated call (code OK) and a rejected one (code UNAUTHENTICATED), the
 * latter confirming the metrics stage runs outermost and still times pipeline rejections.
 */
@QuarkusTest
class GrpcMetricsTest {

  private static final Duration TIMEOUT = Duration.ofSeconds(15);
  private static final String METHOD = SessionServiceGrpc.getGetSessionMethod().getFullMethodName();

  @GrpcClient("session")
  SessionService sessionClient;

  @Inject
  GrpcAuthTestSeeder seeder;

  @Inject
  MeterRegistry registry;

  private double timerCount(String code) {
    Timer timer = registry.find(GrpcMetricsInterceptor.CALLS_TIMER)
        .tag("method", METHOD).tag("code", code).timer();
    return timer == null ? 0d : timer.count();
  }

  @Test
  void recordsTimerForSuccessfulCall() throws Throwable {
    String sid = VertxContextSupport.subscribeAndAwait(() -> seeder.seedValidatedSession());
    double before = timerCount("OK");

    Metadata md = new Metadata();
    md.put(AuthenticationConstants.AUTHORIZATION_HEADER_KEY, "Bearer " + sid);
    GrpcClientUtils.attachHeaders(sessionClient, md)
        .getSession(GetSessionRequest.newBuilder().build()).await().atMost(TIMEOUT);

    Timer timer = registry.find(GrpcMetricsInterceptor.CALLS_TIMER).tag("method", METHOD).tag("code", "OK").timer();
    assertNotNull(timer, "expected a grpc.server.calls timer tagged code=OK for " + METHOD);
    assertTrue(timer.count() > before, "timer count should increase after a successful call");
  }

  @Test
  void recordsTimerForRejectedCall() {
    double before = timerCount("UNAUTHENTICATED");

    // No bearer token: the authn interceptor rejects it; metrics must still record the call.
    sessionClient.getSession(GetSessionRequest.newBuilder().build())
        .subscribe().withSubscriber(UniAssertSubscriber.create()).awaitFailure(TIMEOUT);

    assertTrue(timerCount("UNAUTHENTICATED") > before,
        "metrics stage should time calls rejected by the auth pipeline");
  }
}
