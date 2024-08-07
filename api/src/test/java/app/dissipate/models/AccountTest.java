package app.dissipate.models;

import app.dissipate.data.models.Account;
import app.dissipate.data.models.AccountEmail;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AccountTest {

    @Test
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
