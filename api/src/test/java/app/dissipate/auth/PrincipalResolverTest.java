package app.dissipate.auth;

import app.dissipate.data.models.Account;
import app.dissipate.data.models.AccountRole;
import app.dissipate.data.models.Identity;
import app.dissipate.data.models.Session;
import app.dissipate.exceptions.ApiException;
import app.dissipate.grpc.v1.MethodPolicy;
import app.dissipate.grpc.v1.Role;
import app.dissipate.services.LocalizationService;
import io.grpc.Status;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

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
    PrincipalResolver resolver = new PrincipalResolver();
    resolver.localizationService = new LocalizationService();
    return resolver;
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
  void malformedTokenIsRecoveredAndRejected() {
    try (MockedStatic<Session> mock = Mockito.mockStatic(Session.class)) {
      mock.when(() -> Session.findAuthenticatedBySid("bad"))
          .thenThrow(new IllegalArgumentException("not a uuid"));
      PrincipalResolver resolver = resolver();
      resolver.bind(policy(false, Role.ROLE_USER), "bad");
      assertEquals(Status.Code.UNAUTHENTICATED, failureCode(resolver.authorize()));
    }
  }

  @Test
  void resolvedSessionMeetingRoleYieldsPrincipal() {
    try (MockedStatic<Session> mock = Mockito.mockStatic(Session.class)) {
      mock.when(() -> Session.findAuthenticatedBySid(TOKEN))
          .thenReturn(Uni.createFrom().item(session(AccountRole.VERIFIED)));
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
    try (MockedStatic<Session> mock = Mockito.mockStatic(Session.class)) {
      mock.when(() -> Session.findAuthenticatedBySid(TOKEN))
          .thenReturn(Uni.createFrom().item(session(AccountRole.USER)));
      PrincipalResolver resolver = resolver();
      resolver.bind(policy(false, Role.ROLE_USER), TOKEN);

      Principal first = resolver.authorize().await().indefinitely();
      Principal second = resolver.authorize().await().indefinitely();

      assertSame(first, second);
      // Resolution happened once; the second call short-circuits on the cached principal.
      mock.verify(() -> Session.findAuthenticatedBySid(TOKEN), Mockito.times(1));
    }
  }
}
