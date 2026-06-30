package app.dissipate.auth;

import app.dissipate.data.models.AccountRole;
import app.dissipate.data.models.ApiAppToken;
import app.dissipate.data.models.PrincipalKind;
import app.dissipate.data.models.Session;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The resolved caller of a gRPC method, produced by {@link PrincipalResolver} and used by the
 * auth pipeline to enforce a method's {@code MethodPolicy}. There are two principal kinds:
 *
 * <ul>
 *   <li><b>First-party user</b> — {@link #accountId} set, {@link #appId} null. {@link #role}
 *       drives {@code min_role} checks.</li>
 *   <li><b>Third-party app</b> — {@link #appId} set (Phase 2). {@link #scopes} drive
 *       resource:action checks.</li>
 *   <li><b>Anonymous</b> — everything null/empty; only valid on {@code allow_unauthenticated}
 *       methods.</li>
 * </ul>
 *
 * The {@link #scopes}, {@link #appId} and {@link #rateTier} fields are placeholders wired by
 * Phase 2/3; Phase 1 only ever produces user or anonymous principals.
 */
public record Principal(
    Long accountId,
    Long identityId,
    AccountRole role,
    Set<String> scopes,
    Long appId,
    String rateTier) {

  private static final Principal ANONYMOUS = new Principal(null, null, null, Set.of(), null, null);

  public static Principal anonymous() {
    return ANONYMOUS;
  }

  /** Build a first-party user principal from a resolved, validated session. */
  public static Principal forSession(Session session) {
    Long accountId = session.account != null ? session.account.id : null;
    AccountRole role = session.account != null && session.account.role != null
        ? session.account.role
        : AccountRole.USER;
    Long identityId = session.identity != null ? session.identity.id : null;
    return new Principal(accountId, identityId, role, Set.of(), null, null);
  }

  /** Build a third-party app principal from a resolved access token (scopes parsed from the token). */
  public static Principal forApp(ApiAppToken token) {
    return new Principal(null, null, null, parseScopes(token.scopes), token.apiApp.id, token.apiApp.rateTier);
  }

  /** Parse OAuth space-delimited scopes into a set. */
  static Set<String> parseScopes(String scopes) {
    if (scopes == null || scopes.isBlank()) {
      return Set.of();
    }
    return Arrays.stream(scopes.trim().split("\\s+")).collect(Collectors.toUnmodifiableSet());
  }

  public boolean isAuthenticated() {
    return accountId != null || appId != null;
  }

  public boolean isApp() {
    return appId != null;
  }

  /** Whether this caller a usage counter is attributed to: APP if an app, else USER. */
  public PrincipalKind kind() {
    return isApp() ? PrincipalKind.APP : PrincipalKind.USER;
  }

  /** The id a usage counter is keyed by: app id for apps, account id for users (null if anonymous). */
  public Long meteredId() {
    return isApp() ? appId : accountId;
  }

  public boolean hasScope(String scope) {
    return scopes.contains(scope);
  }

  /** True when this principal's role meets or exceeds {@code minimum}. Apps (no role) never do. */
  public boolean hasRoleAtLeast(AccountRole minimum) {
    return role != null && role.satisfies(minimum);
  }
}
