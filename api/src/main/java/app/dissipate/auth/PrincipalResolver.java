package app.dissipate.auth;

import app.dissipate.constants.AuthenticationConstants;
import app.dissipate.data.models.AccountRole;
import app.dissipate.data.models.ApiAppToken;
import app.dissipate.data.models.ApiUsageCounter;
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
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_FORBIDDEN;
import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_REQUIRED;
import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_SESSION_INVALID;
import static app.dissipate.api.grpc.GrpcErrorCodes.RATE_LIMITED;

/**
 * Stage 2 of the auth pipeline (reactive, per-call). Resolves the caller into a {@link Principal},
 * enforces the method's {@link MethodPolicy} (min_role for users; allow_app + scopes for apps), then
 * enforces the per-minute rate limit and meters the call.
 *
 * <p>A bearer token is either a first-party session sid (a UUID) or a third-party app access token
 * (opaque), disambiguated by UUID-parseability. Methods declaring {@code allow_unauthenticated} skip
 * authorization, rate limiting, and metering for any caller.
 */
@RequestScoped
public class PrincipalResolver {

  @Inject
  LocalizationService localizationService;

  @Inject
  EncryptionUtil encryptionUtil;

  @Inject
  UsageMeterService usageMeterService;

  @Inject
  RateLimitConfig rateLimitConfig;

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

  /** Resolve the principal, enforce the policy + rate limit, and meter the call. Cached per request. */
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
        return admit(Principal.anonymous(), null);
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
          return admit(user, resolved);
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
      return admit(app, null);
    });
  }

  /** A present-but-unresolvable token: anonymous on open methods, otherwise rejected. */
  private Uni<Principal> unresolved(MethodPolicy p, Locale locale) {
    if (p.getAllowUnauthenticated()) {
      return admit(Principal.anonymous(), null);
    }
    return fail(locale, Status.UNAUTHENTICATED, AUTH_SESSION_INVALID);
  }

  /**
   * Final admission: anonymous callers pass through (not limited, not metered); authenticated callers
   * are checked against their per-minute cost ceiling (global current-minute usage across nodes plus
   * this node's un-flushed delta) and, if under it, metered and admitted.
   */
  private Uni<Principal> admit(Principal resolved, Session resolvedSession) {
    if (!resolved.isAuthenticated()) {
      return cache(resolved, resolvedSession);
    }
    Instant minute = Instant.now().truncatedTo(ChronoUnit.MINUTES);
    long limit = rateLimitConfig.limitFor(resolved);
    long callCost = effectiveCost();
    return ApiUsageCounter.findForPrincipalMinute(resolved.kind(), resolved.meteredId(), minute)
        .onItem().transformToUni(rows -> {
          long current = rows.stream().mapToLong(row -> row.cost).sum()
              + usageMeterService.pendingCost(resolved.kind(), resolved.meteredId(), minute);
          if (current >= limit) {
            return fail(currentLocale(), Status.RESOURCE_EXHAUSTED, RATE_LIMITED);
          }
          usageMeterService.record(resolved, callCost);
          return cache(resolved, resolvedSession);
        });
  }

  private Uni<Principal> cache(Principal resolvedPrincipal, Session resolvedSession) {
    this.principal = resolvedPrincipal;
    this.session = resolvedSession;
    return Uni.createFrom().item(resolvedPrincipal);
  }

  /** The session resolved by {@link #authorize()}, or null for anonymous/app callers. */
  public Session session() {
    return session;
  }

  /** The principal resolved by {@link #authorize()}, or null if it has not been called yet. */
  public Principal principal() {
    return principal;
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
