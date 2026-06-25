package app.dissipate.services.grpc;

import app.dissipate.constants.AuthenticationConstants;
import app.dissipate.grpc.v1.GetSessionRequest;
import app.dissipate.grpc.v1.GetSessionResponse;
import app.dissipate.grpc.v1.SessionService;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.grpc.GrpcClientUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * End-to-end check of the gRPC auth pipeline (GrpcAuthenticationInterceptor + PrincipalResolver)
 * against the real database. Seeds a validated session, then drives the SessionService.GetSession
 * method — which declares MethodPolicy(min_role: ROLE_USER) — over the wire with various bearer
 * tokens, asserting the pipeline allows a valid session and rejects everything else.
 */
@QuarkusTest
class GrpcAuthPipelineTest {

  private static final Duration TIMEOUT = Duration.ofSeconds(15);

  @GrpcClient("session")
  SessionService sessionClient;

  @Inject
  GrpcAuthTestSeeder seeder;

  private String sid;

  @BeforeEach
  void seed() throws Throwable {
    // Seeding touches Hibernate Reactive, which needs a Vertx context; run it on one.
    sid = VertxContextSupport.subscribeAndAwait(() -> seeder.seedValidatedSession());
    assertNotNull(sid);
  }

  private SessionService withBearer(String token) {
    Metadata md = new Metadata();
    md.put(AuthenticationConstants.AUTHORIZATION_HEADER_KEY, "Bearer " + token);
    return GrpcClientUtils.attachHeaders(sessionClient, md);
  }

  private Status.Code failureCode(SessionService client) {
    UniAssertSubscriber<GetSessionResponse> sub = client.getSession(GetSessionRequest.newBuilder().build())
        .subscribe().withSubscriber(UniAssertSubscriber.create());
    sub.awaitFailure(TIMEOUT);
    Throwable failure = sub.getFailure();
    assertInstanceOf(StatusRuntimeException.class, failure);
    return ((StatusRuntimeException) failure).getStatus().getCode();
  }

  @Test
  void validSessionPasses() {
    GetSessionResponse response = withBearer(sid).getSession(GetSessionRequest.newBuilder().build())
        .await().atMost(TIMEOUT);
    assertEquals(sid, response.getSid());
  }

  @Test
  void missingTokenIsRejected() {
    // No authorization metadata at all — the sync interceptor must fail-close before the handler.
    assertEquals(Status.Code.UNAUTHENTICATED, failureCode(sessionClient));
  }

  @Test
  void unknownSessionTokenIsRejected() {
    // Well-formed UUID that maps to no validated session — the resolver must reject it.
    assertEquals(Status.Code.UNAUTHENTICATED, failureCode(withBearer(UUID.randomUUID().toString())));
  }

  @Test
  void malformedTokenIsRejected() {
    // Not a UUID — the resolver recovers from the parse failure and rejects as unauthenticated.
    assertEquals(Status.Code.UNAUTHENTICATED, failureCode(withBearer("not-a-uuid")));
  }
}
