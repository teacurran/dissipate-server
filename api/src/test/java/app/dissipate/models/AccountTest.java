package app.dissipate.models;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class AccountTest {

    @Test
    void testEmailConvertsToLower() {
        Account account = new Account();
        account.email = "TEST@TeSt.com";
        Assertions.assertEquals("test@test.com", account.email);
    }
}
