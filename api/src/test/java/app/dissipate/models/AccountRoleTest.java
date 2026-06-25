package app.dissipate.models;

import app.dissipate.data.models.AccountRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountRoleTest {

  @Test
  void roleSatisfiesItself() {
    assertTrue(AccountRole.USER.satisfies(AccountRole.USER));
    assertTrue(AccountRole.VERIFIED.satisfies(AccountRole.VERIFIED));
    assertTrue(AccountRole.ADMIN.satisfies(AccountRole.ADMIN));
  }

  @Test
  void higherRoleSatisfiesLowerMinimum() {
    assertTrue(AccountRole.VERIFIED.satisfies(AccountRole.USER));
    assertTrue(AccountRole.ADMIN.satisfies(AccountRole.USER));
    assertTrue(AccountRole.ADMIN.satisfies(AccountRole.VERIFIED));
  }

  @Test
  void lowerRoleDoesNotSatisfyHigherMinimum() {
    assertFalse(AccountRole.USER.satisfies(AccountRole.VERIFIED));
    assertFalse(AccountRole.USER.satisfies(AccountRole.ADMIN));
    assertFalse(AccountRole.VERIFIED.satisfies(AccountRole.ADMIN));
  }
}
