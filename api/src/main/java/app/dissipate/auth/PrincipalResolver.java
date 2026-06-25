package app.dissipate.auth;

import app.dissipate.constants.AuthenticationConstants;
import app.dissipate.data.models.AccountRole;
import app.dissipate.data.models.Session;
import app.dissipate.grpc.v1.MethodPolicy;
import app.dissipate.grpc.v1.Role;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import app.dissipate.services.LocalizationService;
import io.grpc.Status;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.Locale;

import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_FORBIDDEN;
import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_REQUIRED;
import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_SESSION_INVALID;

/**
 * Stage 2 of the auth pipeline (reactive, per-call). Resolves the caller into a {@link Principal}
 * and enforces the method's {@link MethodPolicy}, which {@link app.dissipate.interceptors.GrpcAuthenticationInterceptor}
 * stashed in the gRPC context.
 *
 * <p>Resolution is deliberately done here rather than in the interceptor so it runs inside the
 * method's own {@code @WithSession} reactive context — the resolved {@link Session} and its
 * fetched account/identity are then managed by the same Hibernate session the handler uses,
 * avoiding cross-session entity errors (the same reason the REST {@code CurrentSession} resolves
 * lazily). Call {@link #authorize()} at the start of every gRPC handler.
 */
@RequestScoped
public class PrincipalResolver {

  @Inject
  LocalizationService localizationService;

  private Principal principal;
  private Session session;

  private MethodPolicy boundPolicy;
  private String boundToken;
  private boolean bound;

  /**
   * Capture the call's policy and bearer token, called synchronously by
   * {@link app.dissipate.interceptors.GrpcAuthenticationInterceptor} while the gRPC context is
   * current. Bound here (rather than read from {@code io.grpc.Context} inside {@link #authorize()})
   * because the request runs inside a @WithSession reactive continuation by then, where the gRPC
   * context is no longer current — but this @RequestScoped bean is propagated across it.
   */
  public void bind(MethodPolicy policy, String token) {
    this.boundPolicy = policy;
    this.boundToken = token;
    this.bound = true;
  }

  /**
   * Resolve the principal and enforce the in-flight method's policy. Caches the result for the
   * request, so calling it more than once per call is cheap. Fails the {@link Uni} with a
   * localized {@code UNAUTHENTICATED} / {@code PERMISSION_DENIED} when the policy is not met.
   */
  public Uni<Principal> authorize() {
    if (principal != null) {
      return Uni.createFrom().item(principal);
    }

    // Prefer values bound synchronously by the interceptor; fall back to the gRPC context for any
    // call path that reaches authorize() without the interceptor having bound (fails closed).
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

    // findAuthenticatedBySid parses the token as a UUID and throws synchronously on a malformed
    // value; defer so that surfaces as a Uni failure we can map cleanly.
    return Uni.createFrom().deferred(() -> Session.findAuthenticatedBySid(token))
        .onFailure(IllegalArgumentException.class).recoverWithItem((Session) null)
        .onItem().transformToUni(resolved -> {
          if (resolved == null) {
            if (p.getAllowUnauthenticated()) {
              return Uni.createFrom().item(cache(Principal.anonymous(), null));
            }
            return fail(locale, Status.UNAUTHENTICATED, AUTH_SESSION_INVALID);
          }
          Principal resolvedPrincipal = Principal.forSession(resolved);
          AccountRole minRole = minRole(p);
          if (minRole != null && !resolvedPrincipal.hasRoleAtLeast(minRole)) {
            return fail(locale, Status.PERMISSION_DENIED, AUTH_FORBIDDEN);
          }
          return Uni.createFrom().item(cache(resolvedPrincipal, resolved));
        });
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
    return resolvedPrincipal;
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
