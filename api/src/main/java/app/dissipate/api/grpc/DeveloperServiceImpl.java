package app.dissipate.api.grpc;

import app.dissipate.auth.PrincipalResolver;
import app.dissipate.auth.ScopeCatalog;
import app.dissipate.data.jpa.UuidGenerator;
import app.dissipate.data.models.ApiApp;
import app.dissipate.data.models.ApiAppStatus;
import app.dissipate.grpc.v1.AppSummary;
import app.dissipate.grpc.v1.DeveloperService;
import app.dissipate.grpc.v1.ListAppsRequest;
import app.dissipate.grpc.v1.ListAppsResponse;
import app.dissipate.grpc.v1.RegisterAppRequest;
import app.dissipate.grpc.v1.RegisterAppResponse;
import app.dissipate.grpc.v1.RotateSecretRequest;
import app.dissipate.grpc.v1.RotateSecretResponse;
import app.dissipate.grpc.v1.SetScopesRequest;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import app.dissipate.interceptors.GrpcSecurityInterceptor;
import app.dissipate.services.LocalizationService;
import app.dissipate.utils.EncryptionUtil;
import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static app.dissipate.api.grpc.GrpcErrorCodes.DEV_APP_NOT_FOUND;
import static app.dissipate.api.grpc.GrpcErrorCodes.DEV_INVALID_SCOPE;

/**
 * Verified-owner management of third-party API apps. The auth pipeline enforces min_role ROLE_VERIFIED;
 * every method then operates only on apps owned by the caller's account. Client secrets are returned
 * in plaintext exactly once (register + rotate) and stored only as SHA-256 hashes.
 */
@GrpcService
@RegisterInterceptor(GrpcSecurityInterceptor.class)
@RegisterInterceptor(GrpcLocaleInterceptor.class)
public class DeveloperServiceImpl implements DeveloperService {

  @Inject
  PrincipalResolver principalResolver;

  @Inject
  UuidGenerator uuidGenerator;

  @Inject
  EncryptionUtil encryptionUtil;

  @Inject
  LocalizationService localizationService;

  @Override
  @WithSession
  public Uni<RegisterAppResponse> registerApp(RegisterAppRequest request) {
    return principalResolver.authorize().onItem().transformToUni(principal -> {
      Locale locale = locale();
      Set<String> unknown = ScopeCatalog.unknown(request.getScopesList());
      if (!unknown.isEmpty()) {
        return fail(locale, Status.INVALID_ARGUMENT, DEV_INVALID_SCOPE);
      }
      String secret = encryptionUtil.generateOpaqueToken();
      ApiApp app = new ApiApp();
      app.id = uuidGenerator.generate();
      app.ownerAccountId = principal.accountId();
      app.clientId = encryptionUtil.generateOpaqueToken();
      app.clientSecretHash = encryptionUtil.sha256(secret);
      app.name = request.getName();
      app.grantedScopes = String.join(" ", request.getScopesList());
      app.status = ApiAppStatus.ACTIVE;
      return app.persistAndFlush().onItem().transform(saved ->
          RegisterAppResponse.newBuilder().setApp(summary(saved)).setClientSecret(secret).build());
    });
  }

  @Override
  @WithSession
  public Uni<ListAppsResponse> listApps(ListAppsRequest request) {
    return principalResolver.authorize().onItem().transformToUni(principal ->
        ApiApp.findByOwner(principal.accountId()).onItem().transform(apps -> {
          ListAppsResponse.Builder rb = ListAppsResponse.newBuilder();
          apps.forEach(app -> rb.addApps(summary(app)));
          return rb.build();
        }));
  }

  @Override
  @WithSession
  public Uni<RotateSecretResponse> rotateSecret(RotateSecretRequest request) {
    return principalResolver.authorize().onItem().transformToUni(principal -> {
      Locale locale = locale();
      UUID appId = parseId(request.getAppId());
      if (appId == null) {
        return fail(locale, Status.NOT_FOUND, DEV_APP_NOT_FOUND);
      }
      return ApiApp.findByIdAndOwner(appId, principal.accountId()).onItem().transformToUni(app -> {
        if (app == null) {
          return fail(locale, Status.NOT_FOUND, DEV_APP_NOT_FOUND);
        }
        String secret = encryptionUtil.generateOpaqueToken();
        app.clientSecretHash = encryptionUtil.sha256(secret);
        return app.persistAndFlush().onItem().transform(saved ->
            RotateSecretResponse.newBuilder().setClientId(saved.clientId).setClientSecret(secret).build());
      });
    });
  }

  @Override
  @WithSession
  public Uni<AppSummary> setScopes(SetScopesRequest request) {
    return principalResolver.authorize().onItem().transformToUni(principal -> {
      Locale locale = locale();
      Set<String> unknown = ScopeCatalog.unknown(request.getScopesList());
      if (!unknown.isEmpty()) {
        return fail(locale, Status.INVALID_ARGUMENT, DEV_INVALID_SCOPE);
      }
      UUID appId = parseId(request.getAppId());
      if (appId == null) {
        return fail(locale, Status.NOT_FOUND, DEV_APP_NOT_FOUND);
      }
      return ApiApp.findByIdAndOwner(appId, principal.accountId()).onItem().transformToUni(app -> {
        if (app == null) {
          return fail(locale, Status.NOT_FOUND, DEV_APP_NOT_FOUND);
        }
        app.grantedScopes = String.join(" ", request.getScopesList());
        return app.persistAndFlush().onItem().transform(this::summary);
      });
    });
  }

  private AppSummary summary(ApiApp app) {
    AppSummary.Builder rb = AppSummary.newBuilder()
        .setId(app.id.toString())
        .setClientId(app.clientId)
        .setRateTier(app.rateTier)
        .setStatus(app.status.name());
    if (app.name != null) {
      rb.setName(app.name);
    }
    if (app.grantedScopes != null && !app.grantedScopes.isBlank()) {
      for (String scope : app.grantedScopes.trim().split("\\s+")) {
        rb.addScopes(scope);
      }
    }
    return rb.build();
  }

  /** Parse a UUID app id, or null if malformed (treated as "no such app"). */
  private static UUID parseId(String appId) {
    try {
      return UUID.fromString(appId.trim());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private static Locale locale() {
    Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();
    return locale != null ? locale : Locale.ENGLISH;
  }

  private <T> Uni<T> fail(Locale locale, Status status, String code) {
    return Uni.createFrom().failure(localizationService.getApiException(locale, status, code));
  }
}
