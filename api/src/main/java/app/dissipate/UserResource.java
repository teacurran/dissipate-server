package app.dissipate;

import app.dissipate.models.Account;
import app.dissipate.models.AccountStatus;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/users")
public class UserResource {

    @GET
    public Uni<Account> hello() {
        return Panache.withTransaction(() -> {
            Account account = new Account();
            account.email = "tea@grilledcheese.com";
            account.status = AccountStatus.ACTIVE;
            return account.persist();
        });
    }

}
