package app.dissipate.models;

import app.dissipate.data.models.Account;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AccountTest {

    @Test
    void testEmailConvertsToLower() {
        Account account = new Account();
        account.email = "TEST@TeSt.com";
        Assertions.assertEquals("test@test.com", account.email);
    }

    @Test
    void testExplicitEmailSetterConvertsToLower() {
        Account account = new Account();
        account.setEmail("TEST@TeSt.com");
        Assertions.assertEquals("test@test.com", account.email);
    }
}
