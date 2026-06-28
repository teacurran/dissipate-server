package app.dissipate.api.grpc;

import app.dissipate.auth.Principal;
import app.dissipate.auth.PrincipalResolver;
import app.dissipate.data.models.Account;
import app.dissipate.data.models.AccountStatus;
import app.dissipate.data.models.Identity;
import app.dissipate.data.models.Session;
import app.dissipate.grpc.v1.GetSessionRequest;
import app.dissipate.grpc.v1.GetSessionResponse;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit coverage for {@link GetSessionMethod}'s response building — the account-status mapping and
 * the null-account / null-identity / null-timestamp branches — by stubbing the resolver's session.
 */
class GetSessionMethodTest {

  private static GetSessionResponse handle(Session session) {
    GetSessionMethod method = new GetSessionMethod();
    PrincipalResolver resolver = Mockito.mock(PrincipalResolver.class);
    Mockito.when(resolver.authorize()).thenReturn(Uni.createFrom().item(Principal.anonymous()));
    Mockito.when(resolver.session()).thenReturn(session);
    method.principalResolver = resolver;
    return method.handler(GetSessionRequest.newBuilder().build()).await().indefinitely();
  }

  private static Session session() {
    Session session = new Session();
    session.id = UUID.randomUUID();
    return session;
  }

  @Test
  void mapsFullSession() {
    Session session = session();
    Account account = new Account();
    account.status = AccountStatus.ACTIVE;
    session.account = account;
    Identity identity = new Identity();
    identity.id = 123L;
    session.identity = identity;
    session.created = Instant.parse("2026-01-02T03:04:05Z");
    session.updated = Instant.parse("2026-01-02T03:05:05Z");

    GetSessionResponse response = handle(session);

    assertEquals(session.id.toString(), response.getSid());
    assertEquals(app.dissipate.grpc.v1.AccountStatus.ACCOUNT_STATUS_ACTIVE, response.getStatus());
    assertEquals(Long.toString(123L, 36), response.getIid());
    assertEquals(Instant.parse("2026-01-02T03:04:05Z").getEpochSecond(), response.getCreated().getSeconds());
    assertTrue(response.hasLastSeen());
  }

  @Test
  void nullAccountMapsToUnspecifiedStatus() {
    GetSessionResponse response = handle(session());
    assertEquals(app.dissipate.grpc.v1.AccountStatus.ACCOUNT_STATUS_UNSPECIFIED, response.getStatus());
  }

  @Test
  void nullAccountStatusMapsToUnspecified() {
    Session session = session();
    session.account = new Account(); // status left null
    GetSessionResponse response = handle(session);
    assertEquals(app.dissipate.grpc.v1.AccountStatus.ACCOUNT_STATUS_UNSPECIFIED, response.getStatus());
  }

  @Test
  void bannedStatusMapsByName() {
    Session session = session();
    Account account = new Account();
    account.status = AccountStatus.BANNED;
    session.account = account;
    GetSessionResponse response = handle(session);
    assertEquals(app.dissipate.grpc.v1.AccountStatus.ACCOUNT_STATUS_BANNED, response.getStatus());
  }

  @Test
  void missingIdentityAndTimestampsOmitted() {
    GetSessionResponse response = handle(session());
    assertEquals("", response.getIid());
    assertFalse(response.hasCreated());
    assertFalse(response.hasLastSeen());
  }
}
