package app.dissipate.auth;

import app.dissipate.data.models.Account;
import app.dissipate.data.models.AccountRole;
import app.dissipate.data.models.Identity;
import app.dissipate.data.models.Session;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrincipalTest {

  @Test
  void anonymousIsUnauthenticatedAndRoleless() {
    Principal p = Principal.anonymous();
    assertFalse(p.isAuthenticated());
    assertFalse(p.isApp());
    assertNull(p.role());
    assertFalse(p.hasRoleAtLeast(AccountRole.USER));
  }

  @Test
  void forSessionCarriesAccountRoleAndIdentity() {
    Account account = new Account();
    account.id = 1001L;
    account.role = AccountRole.VERIFIED;

    Identity identity = new Identity();
    identity.id = 2002L;

    Session session = new Session();
    session.account = account;
    session.identity = identity;

    Principal p = Principal.forSession(session);

    assertTrue(p.isAuthenticated());
    assertFalse(p.isApp());
    assertEquals(1001L, p.accountId());
    assertEquals(2002L, p.identityId());
    assertEquals(AccountRole.VERIFIED, p.role());
    assertTrue(p.hasRoleAtLeast(AccountRole.USER));
    assertTrue(p.hasRoleAtLeast(AccountRole.VERIFIED));
    assertFalse(p.hasRoleAtLeast(AccountRole.ADMIN));
  }

  @Test
  void appPrincipalIsAuthenticatedAndCarriesScopes() {
    Principal app = new Principal(null, null, null, java.util.Set.of("posts:write"), 7L, "tier-1");
    assertTrue(app.isAuthenticated());
    assertTrue(app.isApp());
    assertTrue(app.hasScope("posts:write"));
    assertFalse(app.hasScope("posts:read"));
    assertFalse(app.hasRoleAtLeast(AccountRole.USER));
  }

  @Test
  void forAppParsesScopesIntoSetAndDefaultsEmpty() {
    app.dissipate.data.models.ApiApp app = new app.dissipate.data.models.ApiApp();
    app.id = 5L;
    app.rateTier = "tier";
    app.dissipate.data.models.ApiAppToken token = new app.dissipate.data.models.ApiAppToken();
    token.apiApp = app;

    token.scopes = "posts:read   posts:write";
    Principal multi = Principal.forApp(token);
    assertTrue(multi.isApp());
    assertEquals(5L, multi.appId());
    assertTrue(multi.hasScope("posts:read"));
    assertTrue(multi.hasScope("posts:write"));

    token.scopes = null;
    assertTrue(Principal.forApp(token).scopes().isEmpty());
  }

  @Test
  void forSessionWithoutAccountIsRoleless() {
    Session session = new Session();
    Principal p = Principal.forSession(session);
    assertNull(p.accountId());
    assertEquals(AccountRole.USER, p.role());
    assertFalse(p.isApp());
  }

  @Test
  void forSessionDefaultsToUserWhenRoleUnset() {
    Account account = new Account();
    account.id = 5L;
    account.role = null;

    Session session = new Session();
    session.account = account;

    Principal p = Principal.forSession(session);

    assertEquals(AccountRole.USER, p.role());
    assertNull(p.identityId());
    assertTrue(p.hasRoleAtLeast(AccountRole.USER));
  }
}
