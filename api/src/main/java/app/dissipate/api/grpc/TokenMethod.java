package app.dissipate.api.grpc;

import app.dissipate.data.jpa.UuidGenerator;
import app.dissipate.data.models.ApiApp;
import app.dissipate.data.models.ApiAppToken;
import app.dissipate.grpc.v1.TokenRequest;
import app.dissipate.grpc.v1.TokenResponse;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import app.dissipate.services.LocalizationService;
import app.dissipate.utils.EncryptionUtil;
import io.grpc.Status;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import static app.dissipate.api.grpc.GrpcErrorCodes.OAUTH_INVALID_CLIENT;

/**
 * OAuth2 client-credentials grant. Verifies an {@link ApiApp}'s client_id + client_secret and mints
 * a short-lived opaque access token, storing only its SHA-256 hash. All failures (unknown client,
 * disabled app, wrong secret) return the same generic {@code invalid_client} error so the endpoint
 * does not reveal which client ids exist.
 */
@ApplicationScoped
public class TokenMethod {

  @Inject
  UuidGenerator uuidGenerator;

  @Inject
  EncryptionUtil encryptionUtil;

  @Inject
  LocalizationService localizationService;

  @ConfigProperty(name = "dissipate.oauth.token-ttl", defaultValue = "1h")
  Duration tokenTtl;

  @WithSpan("TokenMethod.token")
  public Uni<TokenResponse> token(TokenRequest request) {
    final Locale locale = currentLocale();
    final String clientId = request.getClientId() == null ? "" : request.getClientId().trim();
    final String clientSecret = request.getClientSecret();

    if (clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty()) {
      return invalidClient(locale);
    }

    return ApiApp.findByClientId(clientId).onItem().transformToUni(app -> {
      if (app == null || !app.isActive()
          || !encryptionUtil.matchesSha256(clientSecret, app.clientSecretHash)) {
        return invalidClient(locale);
      }

      String accessToken = encryptionUtil.generateOpaqueToken();
      ApiAppToken token = new ApiAppToken();
      token.id = uuidGenerator.generate();
      token.apiApp = app;
      token.tokenHash = encryptionUtil.sha256(accessToken);
      token.scopes = app.grantedScopes;
      token.expiresAt = Instant.now().plus(tokenTtl);

      return token.persistAndFlush().onItem().transform(saved ->
          TokenResponse.newBuilder()
              .setAccessToken(accessToken)
              .setTokenType("Bearer")
              .setExpiresIn(tokenTtl.toSeconds())
              .setScope(app.grantedScopes == null ? "" : app.grantedScopes)
              .build());
    });
  }

  private Uni<TokenResponse> invalidClient(Locale locale) {
    return Uni.createFrom().failure(
        localizationService.getApiException(locale, Status.UNAUTHENTICATED, OAUTH_INVALID_CLIENT));
  }

  private static Locale currentLocale() {
    Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();
    return locale != null ? locale : Locale.ENGLISH;
  }
}
