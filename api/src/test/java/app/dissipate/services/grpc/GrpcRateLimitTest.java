package app.dissipate.services.grpc;

import app.dissipate.constants.AuthenticationConstants;
import app.dissipate.grpc.v1.GetSessionRequest;
import app.dissipate.grpc.v1.GetSessionResponse;
import app.dissipate.grpc.v1.SessionService;
import app.dissipate.services.ServerInstance;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.grpc.GrpcClientUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * End-to-end coverage of per-minute rate-limit enforcement: under a tiny user ceiling (3 cost units),
 * the first calls of a session succeed and the one that crosses the ceiling is RESOURCE_EXHAUSTED.
 * Runs under its own profile so the low limit doesn't affect other tests.
 */
@QuarkusTest
@TestProfile(GrpcRateLimitTest.LowLimitProfile.class)
class GrpcRateLimitTest {

  private static final Duration TIMEOUT = Duration.ofSeconds(15);

  @GrpcClient("session")
  SessionService sessionClient;

  @Inject
  GrpcAuthTestSeeder seeder;

  @Inject
  ServerInstance serverInstance;

  // This test runs in its own (restarted) Quarkus instance via @TestProfile; the node registers
  // asynchronously at startup and usage metering keys on the current node id, so wait for it before seeding.
  private void awaitServer() throws InterruptedException {
    long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
    while (System.nanoTime() < deadline) {
      if (serverInstance.getCurrentServer() != null) {
        return;
      }
      Thread.sleep(100);
    }
    throw new IllegalStateException("current server not registered in time");
  }

  @Test
  void exceedingThePerMinuteCeilingIsResourceExhausted() throws Throwable {
    awaitServer();
    String sid = VertxContextSupport.subscribeAndAwait(() -> seeder.seedValidatedSession());
    Metadata md = new Metadata();
    md.put(AuthenticationConstants.AUTHORIZATION_HEADER_KEY, "Bearer " + sid);
    SessionService authed = GrpcClientUtils.attachHeaders(sessionClient, md);

    // GetSession has cost 1; ceiling is 3 -> the first 3 calls are admitted.
    for (int i = 0; i < 3; i++) {
      authed.getSession(GetSessionRequest.newBuilder().build()).await().atMost(TIMEOUT);
    }

    // The 4th call would push the minute's cost to 4 > 3.
    UniAssertSubscriber<GetSessionResponse> sub = authed.getSession(GetSessionRequest.newBuilder().build())
        .subscribe().withSubscriber(UniAssertSubscriber.create());
    sub.awaitFailure(TIMEOUT);
    Throwable failure = sub.getFailure();
    assertInstanceOf(StatusRuntimeException.class, failure);
    assertEquals(Status.Code.RESOURCE_EXHAUSTED, ((StatusRuntimeException) failure).getStatus().getCode());
  }

  public static class LowLimitProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("dissipate.ratelimit.user-per-minute", "3");
    }
  }
}
