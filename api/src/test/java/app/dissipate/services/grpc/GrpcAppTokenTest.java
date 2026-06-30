package app.dissipate.services.grpc;

import app.dissipate.constants.AuthenticationConstants;
import app.dissipate.data.models.ApiAppStatus;
import app.dissipate.grpc.v1.GetSessionRequest;
import app.dissipate.grpc.v1.GetSessionResponse;
import app.dissipate.grpc.v1.OAuthService;
import app.dissipate.grpc.v1.SessionService;
import app.dissipate.grpc.v1.TokenRequest;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.grpc.GrpcClientUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * End-to-end coverage that an OAuth app access token flows through the real auth pipeline: a token
 * obtained from the client-credentials grant authenticates as an app principal and is then refused
 * (PERMISSION_DENIED, not UNAUTHENTICATED) on a first-party-only method — proving both that the app
 * token resolved and that {@code allow_app} is enforced.
 */
@QuarkusTest
class GrpcAppTokenTest {

  private static final Duration TIMEOUT = Duration.ofSeconds(15);
  private static final String SECRET = "app-secret-value";

  @GrpcClient("oauth")
  OAuthService oauthClient;

  @GrpcClient("session")
  SessionService sessionClient;

  @Inject
  GrpcAuthTestSeeder seeder;

  private String mintAppToken() throws Throwable {
    String clientId = "app-" + System.nanoTime();
    VertxContextSupport.subscribeAndAwait(() ->
        seeder.seedApiApp(clientId, SECRET, "posts:read", ApiAppStatus.ACTIVE));
    return oauthClient.token(TokenRequest.newBuilder().setClientId(clientId).setClientSecret(SECRET).build())
        .await().atMost(TIMEOUT).getAccessToken();
  }

  @Test
  void appTokenIsRefusedOnFirstPartyOnlyMethod() throws Throwable {
    String accessToken = mintAppToken();

    Metadata md = new Metadata();
    md.put(AuthenticationConstants.AUTHORIZATION_HEADER_KEY, "Bearer " + accessToken);
    SessionService asApp = GrpcClientUtils.attachHeaders(sessionClient, md);

    UniAssertSubscriber<GetSessionResponse> sub = asApp.getSession(GetSessionRequest.newBuilder().build())
        .subscribe().withSubscriber(UniAssertSubscriber.create());
    sub.awaitFailure(TIMEOUT);
    Throwable failure = sub.getFailure();
    assertInstanceOf(StatusRuntimeException.class, failure);
    // GetSession has no allow_app -> the resolved app principal is denied (not UNAUTHENTICATED).
    assertEquals(Status.Code.PERMISSION_DENIED, ((StatusRuntimeException) failure).getStatus().getCode());
  }
}
