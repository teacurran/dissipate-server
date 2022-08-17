package app.dissipate.models;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AccountStatusTest {

    @Test
    void testStatusActive() {
        Account account = new Account();
        account.status = AccountStatus.ACTIVE;
        Assertions.assertEquals(AccountStatus.ACTIVE, account.status);
    }

    @Test
    void testStatusDisabled() {
        Account account = new Account();
        account.status = AccountStatus.DISABLED;
        Assertions.assertEquals(AccountStatus.DISABLED, account.status);
    }

    @Test
    void testStatusBanned() {
        Account account = new Account();
        account.status = AccountStatus.BANNED;
        Assertions.assertEquals(AccountStatus.BANNED, account.status);
    }

}
