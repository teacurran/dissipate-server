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

  // The timer is recorded server-side in the call's close() callback, which can land just after the
  // client observes the response — so poll rather than read once to avoid a race.
  private boolean awaitTimerAbove(String code, double baseline) {
    long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
    while (System.nanoTime() < deadline) {
      if (timerCount(code) > baseline) {
        return true;
      }
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
    return timerCount(code) > baseline;
  }

  @Test
  void recordsTimerForSuccessfulCall() throws Throwable {
    String sid = VertxContextSupport.subscribeAndAwait(() -> seeder.seedValidatedSession());
    double before = timerCount("OK");

    Metadata md = new Metadata();
    md.put(AuthenticationConstants.AUTHORIZATION_HEADER_KEY, "Bearer " + sid);
    GrpcClientUtils.attachHeaders(sessionClient, md)
        .getSession(GetSessionRequest.newBuilder().build()).await().atMost(TIMEOUT);

    assertTrue(awaitTimerAbove("OK", before),
        "expected the grpc.server.calls timer tagged code=OK to increase after a successful call");
  }

  @Test
  void recordsTimerForRejectedCall() {
    double before = timerCount("UNAUTHENTICATED");

    // No bearer token: the authn interceptor rejects it; metrics must still record the call.
    sessionClient.getSession(GetSessionRequest.newBuilder().build())
        .subscribe().withSubscriber(UniAssertSubscriber.create()).awaitFailure(TIMEOUT);

    assertTrue(awaitTimerAbove("UNAUTHENTICATED", before),
        "metrics stage should time calls rejected by the auth pipeline");
  }
}
