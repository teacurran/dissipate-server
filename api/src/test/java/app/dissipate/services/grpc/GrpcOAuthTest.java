package app.dissipate.services.grpc;

import app.dissipate.data.models.ApiAppStatus;
import app.dissipate.grpc.v1.OAuthService;
import app.dissipate.grpc.v1.TokenRequest;
import app.dissipate.grpc.v1.TokenResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * End-to-end coverage for the OAuth2 client-credentials grant (OAuthService.Token): a registered,
 * active app exchanges its credentials for a Bearer token; wrong secret / unknown client / disabled
 * app are all refused with the same generic UNAUTHENTICATED; blank input fails validation.
 */
@QuarkusTest
class GrpcOAuthTest {

  private static final Duration TIMEOUT = Duration.ofSeconds(15);
  private static final String SECRET = "s3cr3t-" + "value-do-not-log";
  private static final String SCOPES = "posts:read posts:write";
  private static final AtomicInteger COUNTER = new AtomicInteger();

  @GrpcClient("oauth")
  OAuthService oauthClient;

  @Inject
  GrpcAuthTestSeeder seeder;

  private String registerApp(ApiAppStatus status) throws Throwable {
    String clientId = "client-" + COUNTER.incrementAndGet() + "-" + System.nanoTime();
    VertxContextSupport.subscribeAndAwait(() -> seeder.seedApiApp(clientId, SECRET, SCOPES, status));
    return clientId;
  }

  private Status.Code grantFailureCode(String clientId, String secret) {
    UniAssertSubscriber<TokenResponse> sub = oauthClient
        .token(TokenRequest.newBuilder().setClientId(clientId).setClientSecret(secret).build())
        .subscribe().withSubscriber(UniAssertSubscriber.create());
    sub.awaitFailure(TIMEOUT);
    Throwable failure = sub.getFailure();
    assertInstanceOf(StatusRuntimeException.class, failure);
    return ((StatusRuntimeException) failure).getStatus().getCode();
  }

  @Test
  void validCredentialsYieldABearerToken() throws Throwable {
    String clientId = registerApp(ApiAppStatus.ACTIVE);

    TokenResponse response = oauthClient
        .token(TokenRequest.newBuilder().setClientId(clientId).setClientSecret(SECRET).build())
        .await().atMost(TIMEOUT);

    assertFalse(response.getAccessToken().isBlank());
    assertEquals("Bearer", response.getTokenType());
    assertEquals(3600L, response.getExpiresIn());
    assertEquals(SCOPES, response.getScope());
  }

  @Test
  void grantWithNoScopesReturnsEmptyScope() throws Throwable {
    String clientId = "client-" + COUNTER.incrementAndGet() + "-" + System.nanoTime();
    VertxContextSupport.subscribeAndAwait(() -> seeder.seedApiApp(clientId, SECRET, null, ApiAppStatus.ACTIVE));

    TokenResponse response = oauthClient
        .token(TokenRequest.newBuilder().setClientId(clientId).setClientSecret(SECRET).build())
        .await().atMost(TIMEOUT);

    assertEquals("", response.getScope());
  }

  @Test
  void wrongSecretIsRejected() throws Throwable {
    String clientId = registerApp(ApiAppStatus.ACTIVE);
    assertEquals(Status.Code.UNAUTHENTICATED, grantFailureCode(clientId, "wrong-secret"));
  }

  @Test
  void unknownClientIsRejected() {
    assertEquals(Status.Code.UNAUTHENTICATED,
        grantFailureCode("client-does-not-exist-" + System.nanoTime(), SECRET));
  }

  @Test
  void disabledAppIsRejected() throws Throwable {
    String clientId = registerApp(ApiAppStatus.DISABLED);
    assertEquals(Status.Code.UNAUTHENTICATED, grantFailureCode(clientId, SECRET));
  }

  @Test
  void blankClientIdFailsValidation() {
    // client_id has (buf.validate.field).string.min_len = 1.
    assertEquals(Status.Code.INVALID_ARGUMENT, grantFailureCode("", SECRET));
  }
}
