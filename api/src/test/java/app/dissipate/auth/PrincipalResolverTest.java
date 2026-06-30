package app.dissipate.auth;

import app.dissipate.data.models.Account;
import app.dissipate.data.models.AccountRole;
import app.dissipate.data.models.ApiApp;
import app.dissipate.data.models.ApiAppStatus;
import app.dissipate.data.models.ApiAppToken;
import app.dissipate.data.models.ApiUsageCounter;
import app.dissipate.data.models.Identity;
import app.dissipate.data.models.PrincipalKind;
import app.dissipate.data.models.Session;
import app.dissipate.exceptions.ApiException;
import app.dissipate.grpc.v1.MethodPolicy;
import app.dissipate.grpc.v1.Role;
import app.dissipate.services.LocalizationService;
import app.dissipate.services.UsageMeterService;
import app.dissipate.utils.EncryptionUtil;
import io.grpc.Status;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit coverage for {@link PrincipalResolver#authorize()} across every policy/token branch, driving
 * the session lookup via a static mock so no database or gRPC context is required.
 */
class PrincipalResolverTest {

  private static final String TOKEN = UUID.randomUUID().toString();

  private static PrincipalResolver resolver() {
    return resolver(1_000_000L); // effectively unlimited unless a test overrides
  }

  private static PrincipalResolver resolver(long limit) {
    PrincipalResolver resolver = new PrincipalResolver();
    resolver.localizationService = new LocalizationService();
    resolver.encryptionUtil = new EncryptionUtil();
    resolver.usageMeterService = new UsageMeterService();
    resolver.rateLimitConfig = limitConfig(limit);
    return resolver;
  }

  private static RateLimitConfig limitConfig(long limit) {
    return new RateLimitConfig() {
      @Override public long userPerMinute() { return limit; }
      @Override public long appDefaultPerMinute() { return limit; }
      @Override public Map<String, Long> tier() { return Map.of(); }
    };
  }

  /** Stub the global usage read with the given existing rows (empty = no prior usage). */
  private static void stubUsage(MockedStatic<ApiUsageCounter> mock, List<ApiUsageCounter> rows) {
    mock.when(() -> ApiUsageCounter.findForPrincipalMinute(
        ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Uni.createFrom().item(rows));
  }

  private static ApiUsageCounter usageRow(long cost) {
    ApiUsageCounter row = new ApiUsageCounter();
    row.cost = cost;
    return row;
  }

  /** A non-UUID token so authorize() routes to the app-token path. */
  private static final String APP_TOKEN = "opaque-app-token-not-a-uuid";

  private static ApiAppToken appToken(String scopes, ApiAppStatus status, Instant expiresAt) {
    ApiApp app = new ApiApp();
    app.id = 7L;
    app.status = status;
    app.rateTier = "default";
    ApiAppToken token = new ApiAppToken();
    token.apiApp = app;
    token.scopes = scopes;
    token.expiresAt = expiresAt;
    return token;
  }

  private static void stubAppToken(MockedStatic<ApiAppToken> mock, ApiAppToken token) {
    mock.when(() -> ApiAppToken.findActiveByHash(ArgumentMatchers.anyString()))
        .thenReturn(Uni.createFrom().item(token));
  }

  private static MethodPolicy policy(boolean allowUnauthenticated, Role minRole) {
    return MethodPolicy.newBuilder().setAllowUnauthenticated(allowUnauthenticated).setMinRole(minRole).build();
  }

  private static Session session(AccountRole role) {
    Account account = new Account();
    account.id = 4242L;
    account.role = role;
    Identity identity = new Identity();
    identity.id = 99L;
    Session session = new Session();
    session.account = account;
    session.identity = identity;
    return session;
  }

  private static Status.Code failureCode(Uni<Principal> uni) {
    UniAssertSubscriber<Principal> sub = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    sub.awaitFailure();
    Throwable failure = sub.getFailure();
    assertInstanceOf(ApiException.class, failure);
    return ((ApiException) failure).getStatus().getCode();
  }

  @Test
  void blankTokenOnUnauthenticatedMethodYieldsAnonymous() {
    PrincipalResolver resolver = resolver();
    resolver.bind(policy(true, Role.ROLE_UNSPECIFIED), "");

    Principal principal = resolver.authorize().subscribe().withSubscriber(UniAssertSubscriber.create())
        .awaitItem().getItem();

    assertSame(Principal.anonymous(), principal);
    assertNull(resolver.session());
    assertSame(principal, resolver.principal());
  }

  @Test
  void blankTokenOnProtectedMethodIsUnauthenticated() {
    PrincipalResolver resolver = resolver();
    resolver.bind(policy(false, Role.ROLE_USER), "");
    assertEquals(Status.Code.UNAUTHENTICATED, failureCode(resolver.authorize()));
  }

  @Test
  void unknownSessionOnProtectedMethodIsUnauthenticated() {
    try (MockedStatic<Session> mock = Mockito.mockStatic(Session.class)) {
      mock.when(() -> Session.findAuthenticatedBySid(TOKEN)).thenReturn(Uni.createFrom().nullItem());
      PrincipalResolver resolver = resolver();
      resolver.bind(policy(false, Role.ROLE_USER), TOKEN);
      assertEquals(Status.Code.UNAUTHENTICATED, failureCode(resolver.authorize()));
    }
  }

  @Test
  void unknownSessionOnUnauthenticatedMethodYieldsAnonymous() {
    try (MockedStatic<Session> mock = Mockito.mockStatic(Session.class)) {
      mock.when(() -> Session.findAuthenticatedBySid(TOKEN)).thenReturn(Uni.createFrom().nullItem());
      PrincipalResolver resolver = resolver();
      resolver.bind(policy(true, Role.ROLE_UNSPECIFIED), TOKEN);
      Principal principal = resolver.authorize().subscribe().withSubscriber(UniAssertSubscriber.create())
          .awaitItem().getItem();
      assertSame(Principal.anonymous(), principal);
    }
  }

  @Test
  void unknownAppTokenIsRejected() {
    try (MockedStatic<ApiAppToken> mock = Mockito.mockStatic(ApiAppToken.class)) {
      mock.when(() -> ApiAppToken.findActiveByHash(ArgumentMatchers.anyString()))
          .thenReturn(Uni.createFrom().nullItem());
      PrincipalResolver resolver = resolver();
      resolver.bind(policy(false, Role.ROLE_USER), APP_TOKEN);
      assertEquals(Status.Code.UNAUTHENTICATED, failureCode(resolver.authorize()));
    }
  }

  @Test
  void appTokenWithAllowAppAndRequiredScopeYieldsAppPrincipal() {
    try (MockedStatic<ApiAppToken> mock = Mockito.mockStatic(ApiAppToken.class);
         MockedStatic<ApiUsageCounter> usage = Mockito.mockStatic(ApiUsageCounter.class)) {
      stubAppToken(mock, appToken("posts:read posts:write", ApiAppStatus.ACTIVE, Instant.now().plusSeconds(3600)));
      stubUsage(usage, List.of());
      PrincipalResolver resolver = resolver();
      MethodPolicy appPolicy = MethodPolicy.newBuilder().setAllowApp(true).addScopes("posts:read").build();
      resolver.bind(appPolicy, APP_TOKEN);

      Principal principal = resolver.authorize().subscribe().withSubscriber(UniAssertSubscriber.create())
          .awaitItem().getItem();

      assertTrue(principal.isApp());
      assertEquals(7L, principal.appId());
      assertTrue(principal.hasScope("posts:write"));
      assertNull(resolver.session());
    }
  }

  @Test
  void appTokenMissingRequiredScopeIsDenied() {
    try (MockedStatic<ApiAppToken> mock = Mockito.mockStatic(ApiAppToken.class)) {
      stubAppToken(mock, appToken("posts:read", ApiAppStatus.ACTIVE, Instant.now().plusSeconds(3600)));
      PrincipalResolver resolver = resolver();
      MethodPolicy appPolicy = MethodPolicy.newBuilder().setAllowApp(true).addScopes("posts:delete").build();
      resolver.bind(appPolicy, APP_TOKEN);
      assertEquals(Status.Code.PERMISSION_DENIED, failureCode(resolver.authorize()));
    }
  }

  @Test
  void appTokenOnUserOnlyMethodIsDenied() {
    try (MockedStatic<ApiAppToken> mock = Mockito.mockStatic(ApiAppToken.class)) {
      stubAppToken(mock, appToken("posts:read", ApiAppStatus.ACTIVE, Instant.now().plusSeconds(3600)));
      PrincipalResolver resolver = resolver();
      // min_role USER, allow_app defaults false -> apps may not call it.
      resolver.bind(policy(false, Role.ROLE_USER), APP_TOKEN);
      assertEquals(Status.Code.PERMISSION_DENIED, failureCode(resolver.authorize()));
    }
  }

  @Test
  void expiredAppTokenIsRejected() {
    try (MockedStatic<ApiAppToken> mock = Mockito.mockStatic(ApiAppToken.class)) {
      stubAppToken(mock, appToken("posts:read", ApiAppStatus.ACTIVE, Instant.now().minusSeconds(1)));
      PrincipalResolver resolver = resolver();
      resolver.bind(MethodPolicy.newBuilder().setAllowApp(true).build(), APP_TOKEN);
      assertEquals(Status.Code.UNAUTHENTICATED, failureCode(resolver.authorize()));
    }
  }

  @Test
  void tokenOfDisabledAppIsRejected() {
    try (MockedStatic<ApiAppToken> mock = Mockito.mockStatic(ApiAppToken.class)) {
      stubAppToken(mock, appToken("posts:read", ApiAppStatus.DISABLED, Instant.now().plusSeconds(3600)));
      PrincipalResolver resolver = resolver();
      resolver.bind(MethodPolicy.newBuilder().setAllowApp(true).build(), APP_TOKEN);
      assertEquals(Status.Code.UNAUTHENTICATED, failureCode(resolver.authorize()));
    }
  }

  @Test
  void appTokenOnUnauthenticatedMethodSkipsEnforcement() {
    try (MockedStatic<ApiAppToken> mock = Mockito.mockStatic(ApiAppToken.class);
         MockedStatic<ApiUsageCounter> usage = Mockito.mockStatic(ApiUsageCounter.class)) {
      stubAppToken(mock, appToken("posts:read", ApiAppStatus.ACTIVE, Instant.now().plusSeconds(3600)));
      stubUsage(usage, List.of());
      PrincipalResolver resolver = resolver();
      // allow_unauthenticated method: a valid app token is accepted without allow_app/scope checks.
      resolver.bind(policy(true, Role.ROLE_UNSPECIFIED), APP_TOKEN);
      Principal principal = resolver.authorize().subscribe().withSubscriber(UniAssertSubscriber.create())
          .awaitItem().getItem();
      assertTrue(principal.isApp());
    }
  }

  @Test
  void resolvedSessionMeetingRoleYieldsPrincipal() {
    try (MockedStatic<Session> mock = Mockito.mockStatic(Session.class);
         MockedStatic<ApiUsageCounter> usage = Mockito.mockStatic(ApiUsageCounter.class)) {
      mock.when(() -> Session.findAuthenticatedBySid(TOKEN))
          .thenReturn(Uni.createFrom().item(session(AccountRole.VERIFIED)));
      stubUsage(usage, List.of());
      PrincipalResolver resolver = resolver();
      resolver.bind(policy(false, Role.ROLE_VERIFIED), TOKEN);

      Principal principal = resolver.authorize().subscribe().withSubscriber(UniAssertSubscriber.create())
          .awaitItem().getItem();

      assertEquals(4242L, principal.accountId());
      assertEquals(99L, principal.identityId());
      assertTrue(principal.hasRoleAtLeast(AccountRole.VERIFIED));
      assertEquals(AccountRole.VERIFIED, resolver.session().account.role);
    }
  }

  @Test
  void resolvedSessionBelowRequiredRoleIsPermissionDenied() {
    try (MockedStatic<Session> mock = Mockito.mockStatic(Session.class)) {
      mock.when(() -> Session.findAuthenticatedBySid(TOKEN))
          .thenReturn(Uni.createFrom().item(session(AccountRole.USER)));
      PrincipalResolver resolver = resolver();
      resolver.bind(policy(false, Role.ROLE_ADMIN), TOKEN);
      assertEquals(Status.Code.PERMISSION_DENIED, failureCode(resolver.authorize()));
    }
  }

  @Test
  void unboundResolverFallsBackToGrpcContext() {
    // When the interceptor hasn't bind()-ed (defensive path), authorize() reads policy + token from
    // the gRPC context instead.
    io.grpc.Context ctx = io.grpc.Context.current()
        .withValue(app.dissipate.constants.AuthenticationConstants.POLICY_KEY, policy(true, Role.ROLE_UNSPECIFIED))
        .withValue(app.dissipate.constants.AuthenticationConstants.BEARER_TOKEN_KEY, "");
    io.grpc.Context previous = ctx.attach();
    try {
      Principal principal = resolver().authorize().subscribe().withSubscriber(UniAssertSubscriber.create())
          .awaitItem().getItem();
      assertSame(Principal.anonymous(), principal);
    } finally {
      ctx.detach(previous);
    }
  }

  @Test
  void unboundWithNoContextPolicyFailsClosed() {
    // No bind(), no policy in context -> the fail-closed DEFAULT_POLICY (auth required) applies.
    assertEquals(Status.Code.UNAUTHENTICATED, failureCode(resolver().authorize()));
  }

  @Test
  void authorizeIsCachedAcrossCalls() {
    try (MockedStatic<Session> mock = Mockito.mockStatic(Session.class);
         MockedStatic<ApiUsageCounter> usage = Mockito.mockStatic(ApiUsageCounter.class)) {
      mock.when(() -> Session.findAuthenticatedBySid(TOKEN))
          .thenReturn(Uni.createFrom().item(session(AccountRole.USER)));
      stubUsage(usage, List.of());
      PrincipalResolver resolver = resolver();
      resolver.bind(policy(false, Role.ROLE_USER), TOKEN);

      Principal first = resolver.authorize().await().indefinitely();
      Principal second = resolver.authorize().await().indefinitely();

      assertSame(first, second);
      // Resolution happened once; the second call short-circuits on the cached principal.
      mock.verify(() -> Session.findAuthenticatedBySid(TOKEN), Mockito.times(1));
    }
  }

  @Test
  void callAtTheRateLimitIsResourceExhausted() {
    try (MockedStatic<Session> mock = Mockito.mockStatic(Session.class);
         MockedStatic<ApiUsageCounter> usage = Mockito.mockStatic(ApiUsageCounter.class)) {
      mock.when(() -> Session.findAuthenticatedBySid(TOKEN))
          .thenReturn(Uni.createFrom().item(session(AccountRole.USER)));
      stubUsage(usage, List.of(usageRow(5))); // already at the limit of 5
      PrincipalResolver resolver = resolver(5);
      resolver.bind(policy(false, Role.ROLE_USER), TOKEN);
      assertEquals(Status.Code.RESOURCE_EXHAUSTED, failureCode(resolver.authorize()));
    }
  }

  @Test
  void callUnderTheRateLimitIsAdmitted() {
    try (MockedStatic<Session> mock = Mockito.mockStatic(Session.class);
         MockedStatic<ApiUsageCounter> usage = Mockito.mockStatic(ApiUsageCounter.class)) {
      mock.when(() -> Session.findAuthenticatedBySid(TOKEN))
          .thenReturn(Uni.createFrom().item(session(AccountRole.USER)));
      stubUsage(usage, List.of(usageRow(4))); // under the limit of 5
      PrincipalResolver resolver = resolver(5);
      resolver.bind(policy(false, Role.ROLE_USER), TOKEN);

      Principal principal = resolver.authorize().subscribe().withSubscriber(UniAssertSubscriber.create())
          .awaitItem().getItem();
      assertEquals(4242L, principal.accountId());
    }
  }
}
