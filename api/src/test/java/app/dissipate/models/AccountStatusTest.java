package app.dissipate.models;

import app.dissipate.data.models.Account;
import app.dissipate.data.models.AccountStatusEnum;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AccountStatusTest {

    @Test
    void testStatusActive() {
        Account account = new Account();
        account.status = AccountStatusEnum.ACTIVE;
        Assertions.assertEquals(AccountStatusEnum.ACTIVE, account.status);
    }

    @Test
    void testStatusDisabled() {
        Account account = new Account();
        account.status = AccountStatusEnum.DISABLED;
        Assertions.assertEquals(AccountStatusEnum.DISABLED, account.status);
    }

    @Test
    void testStatusBanned() {
        Account account = new Account();
        account.status = AccountStatusEnum.BANNED;
        Assertions.assertEquals(AccountStatusEnum.BANNED, account.status);
    }

}
