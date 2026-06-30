package app.dissipate.auth;

import app.dissipate.constants.AuthenticationConstants;
import app.dissipate.data.models.AccountRole;
import app.dissipate.data.models.ApiAppToken;
import app.dissipate.data.models.Session;
import app.dissipate.grpc.v1.MethodPolicy;
import app.dissipate.grpc.v1.Role;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import app.dissipate.services.LocalizationService;
import app.dissipate.services.UsageMeterService;
import app.dissipate.utils.EncryptionUtil;
import io.grpc.Status;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_FORBIDDEN;
import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_REQUIRED;
import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_SESSION_INVALID;

/**
 * Stage 2 of the auth pipeline (reactive, per-call). Resolves the caller into a {@link Principal} and
 * enforces the method's {@link MethodPolicy}, which {@link app.dissipate.interceptors.GrpcAuthenticationInterceptor}
 * bound into this request-scoped bean.
 *
 * <p>A bearer token is either a first-party session sid (a UUID) or a third-party app access token
 * (opaque). They are disambiguated by UUID-parseability and resolved against the session table or the
 * {@code api_app_tokens} table respectively. Authorization then diverges by principal kind:
 * <ul>
 *   <li><b>user</b> — must meet {@code min_role};</li>
 *   <li><b>app</b> — the method must set {@code allow_app}, and the token must hold every required
 *       {@code scope}.</li>
 * </ul>
 * Methods declaring {@code allow_unauthenticated} skip enforcement entirely (any caller is allowed).
 */
@RequestScoped
public class PrincipalResolver {

  @Inject
  LocalizationService localizationService;

  @Inject
  EncryptionUtil encryptionUtil;

  @Inject
  UsageMeterService usageMeterService;

  private Principal principal;
  private Session session;

  private MethodPolicy boundPolicy;
  private String boundToken;
  private boolean bound;

  /**
   * Capture the call's policy and bearer token, called synchronously by the authn interceptor while
   * the gRPC context is current (the handler later runs in a @WithSession reactive continuation where
   * that context is gone, but this @RequestScoped bean is propagated across it).
   */
  public void bind(MethodPolicy policy, String token) {
    this.boundPolicy = policy;
    this.boundToken = token;
    this.bound = true;
  }

  /** Resolve the principal and enforce the in-flight method's policy. Cached for the request. */
  public Uni<Principal> authorize() {
    if (principal != null) {
      return Uni.createFrom().item(principal);
    }

    MethodPolicy policy = bound ? boundPolicy : AuthenticationConstants.POLICY_KEY.get();
    if (policy == null) {
      policy = MethodPolicyResolver.DEFAULT_POLICY;
    }
    final MethodPolicy p = policy;
    final String token = bound ? boundToken : AuthenticationConstants.BEARER_TOKEN_KEY.get();
    final Locale locale = currentLocale();

    if (token == null || token.isBlank()) {
      if (p.getAllowUnauthenticated()) {
        return Uni.createFrom().item(cache(Principal.anonymous(), null));
      }
      return fail(locale, Status.UNAUTHENTICATED, AUTH_REQUIRED);
    }

    // A session sid is a UUID; anything else is treated as an opaque app access token.
    return isUuid(token) ? resolveUser(token, p, locale) : resolveApp(token, p, locale);
  }

  private Uni<Principal> resolveUser(String token, MethodPolicy p, Locale locale) {
    return Uni.createFrom().deferred(() -> Session.findAuthenticatedBySid(token))
        .onFailure(IllegalArgumentException.class).recoverWithItem((Session) null)
        .onItem().transformToUni(resolved -> {
          if (resolved == null) {
            return unresolved(p, locale);
          }
          Principal user = Principal.forSession(resolved);
          AccountRole minRole = minRole(p);
          if (minRole != null && !user.hasRoleAtLeast(minRole)) {
            return fail(locale, Status.PERMISSION_DENIED, AUTH_FORBIDDEN);
          }
          return Uni.createFrom().item(cache(user, resolved));
        });
  }

  private Uni<Principal> resolveApp(String token, MethodPolicy p, Locale locale) {
    String tokenHash = encryptionUtil.sha256(token);
    return ApiAppToken.findActiveByHash(tokenHash).onItem().transformToUni(appToken -> {
      if (appToken == null || appToken.isExpired(Instant.now())
          || appToken.apiApp == null || !appToken.apiApp.isActive()) {
        return unresolved(p, locale);
      }
      Principal app = Principal.forApp(appToken);
      if (!p.getAllowUnauthenticated()) {
        if (!p.getAllowApp()) {
          return fail(locale, Status.PERMISSION_DENIED, AUTH_FORBIDDEN);
        }
        for (String required : p.getScopesList()) {
          if (!app.hasScope(required)) {
            return fail(locale, Status.PERMISSION_DENIED, AUTH_FORBIDDEN);
          }
        }
      }
      return Uni.createFrom().item(cache(app, null));
    });
  }

  /** A present-but-unresolvable token: anonymous on open methods, otherwise rejected. */
  private Uni<Principal> unresolved(MethodPolicy p, Locale locale) {
    if (p.getAllowUnauthenticated()) {
      return Uni.createFrom().item(cache(Principal.anonymous(), null));
    }
    return fail(locale, Status.UNAUTHENTICATED, AUTH_SESSION_INVALID);
  }

  /** The session resolved by {@link #authorize()}, or null for anonymous/app callers. */
  public Session session() {
    return session;
  }

  /** The principal resolved by {@link #authorize()}, or null if it has not been called yet. */
  public Principal principal() {
    return principal;
  }

  private Principal cache(Principal resolvedPrincipal, Session resolvedSession) {
    this.principal = resolvedPrincipal;
    this.session = resolvedSession;
    // Meter the call once, here at resolution (not on cached re-reads). No-op for anonymous.
    usageMeterService.record(resolvedPrincipal, effectiveCost());
    return resolvedPrincipal;
  }

  /** The in-flight method's rate-limit weight; 0/absent is treated as the default weight of 1. */
  private long effectiveCost() {
    MethodPolicy policy = bound ? boundPolicy : AuthenticationConstants.POLICY_KEY.get();
    if (policy == null) {
      return 1;
    }
    int cost = policy.getCost();
    return cost <= 0 ? 1 : cost;
  }

  private static boolean isUuid(String token) {
    try {
      UUID.fromString(token);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /** Translate a policy's {@code min_role} into the domain role, or null when no floor is set. */
  private static AccountRole minRole(MethodPolicy policy) {
    if (policy.getAllowUnauthenticated()) {
      return null;
    }
    return switch (policy.getMinRole()) {
      case ROLE_USER -> AccountRole.USER;
      case ROLE_VERIFIED -> AccountRole.VERIFIED;
      case ROLE_ADMIN -> AccountRole.ADMIN;
      default -> null;
    };
  }

  private static Locale currentLocale() {
    Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();
    return locale != null ? locale : Locale.ENGLISH;
  }

  private <T> Uni<T> fail(Locale locale, Status status, String code) {
    return Uni.createFrom().failure(localizationService.getApiException(locale, status, code));
  }
}
