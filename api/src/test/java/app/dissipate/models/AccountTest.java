package app.dissipate.models;

import app.dissipate.data.models.AccountEmail;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class AccountTest {

    @Test
    @Disabled("AccountEmail does not automatically convert to lowercase - needs implementation")
    void testEmailConvertsToLower() {
      AccountEmail account = new AccountEmail();
        account.email = "TEST@TeSt.com";
        Assertions.assertEquals("test@test.com", account.email);
    }

    @Test
    void testExplicitEmailSetterConvertsToLower() {
      AccountEmail account = new AccountEmail();
        account.setEmail("TEST@TeSt.com");
        Assertions.assertEquals("test@test.com", account.email);
    }
}
