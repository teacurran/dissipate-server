package app.dissipate.api.grpc;

import app.dissipate.data.jpa.UuidGenerator;
import app.dissipate.data.models.ApiApp;
import app.dissipate.data.models.ApiAppStatus;
import app.dissipate.exceptions.ApiException;
import app.dissipate.grpc.v1.TokenRequest;
import app.dissipate.grpc.v1.TokenResponse;
import app.dissipate.services.LocalizationService;
import app.dissipate.utils.EncryptionUtil;
import io.grpc.Status;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Unit coverage for {@link TokenMethod}'s reject paths (blank input, unknown client, disabled app,
 * wrong secret), all of which must surface the same generic {@code invalid_client} UNAUTHENTICATED
 * without enumeration. The success path persists a token and is covered end-to-end by GrpcOAuthTest.
 */
class TokenMethodTest {

  private static final EncryptionUtil ENC = new EncryptionUtil();

  private static TokenMethod method() {
    TokenMethod method = new TokenMethod();
    method.encryptionUtil = ENC;
    method.localizationService = new LocalizationService();
    method.uuidGenerator = Mockito.mock(UuidGenerator.class);
    method.tokenTtl = Duration.ofHours(1);
    return method;
  }

  private static TokenRequest request(String clientId, String secret) {
    return TokenRequest.newBuilder().setClientId(clientId).setClientSecret(secret).build();
  }

  private static Status.Code failureCode(Uni<TokenResponse> uni) {
    UniAssertSubscriber<TokenResponse> sub = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    sub.awaitFailure();
    Throwable failure = sub.getFailure();
    assertInstanceOf(ApiException.class, failure);
    return ((ApiException) failure).getStatus().getCode();
  }

  private static ApiApp app(ApiAppStatus status, String secret) {
    ApiApp app = new ApiApp();
    app.clientId = "c";
    app.status = status;
    app.clientSecretHash = ENC.sha256(secret);
    app.grantedScopes = "posts:read";
    return app;
  }

  @Test
  void blankClientIdIsRejected() {
    assertEquals(Status.Code.UNAUTHENTICATED, failureCode(method().token(request("", "secret"))));
  }

  @Test
  void blankSecretIsRejected() {
    assertEquals(Status.Code.UNAUTHENTICATED, failureCode(method().token(request("c", ""))));
  }

  @Test
  void unknownClientIsRejected() {
    try (MockedStatic<ApiApp> mock = Mockito.mockStatic(ApiApp.class)) {
      mock.when(() -> ApiApp.findByClientId("c")).thenReturn(Uni.createFrom().nullItem());
      assertEquals(Status.Code.UNAUTHENTICATED, failureCode(method().token(request("c", "secret"))));
    }
  }

  @Test
  void disabledAppIsRejected() {
    try (MockedStatic<ApiApp> mock = Mockito.mockStatic(ApiApp.class)) {
      mock.when(() -> ApiApp.findByClientId("c"))
          .thenReturn(Uni.createFrom().item(app(ApiAppStatus.DISABLED, "secret")));
      assertEquals(Status.Code.UNAUTHENTICATED, failureCode(method().token(request("c", "secret"))));
    }
  }

  @Test
  void wrongSecretIsRejected() {
    try (MockedStatic<ApiApp> mock = Mockito.mockStatic(ApiApp.class)) {
      mock.when(() -> ApiApp.findByClientId("c"))
          .thenReturn(Uni.createFrom().item(app(ApiAppStatus.ACTIVE, "right-secret")));
      assertEquals(Status.Code.UNAUTHENTICATED, failureCode(method().token(request("c", "wrong-secret"))));
    }
  }
}
