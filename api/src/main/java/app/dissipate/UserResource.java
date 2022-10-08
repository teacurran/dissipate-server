package app.dissipate;

import app.dissipate.data.models.Account;
import app.dissipate.data.models.AccountStatus;
import app.dissipate.data.models.Identity;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/users")
public class UserResource {

    @GET
    public Uni<Identity> hello() {
        return Panache.withTransaction(() -> {
            Account account = new Account();
            account.email = "tea@grilledcheese.com";
            account.status = AccountStatus.ACTIVE;

            Identity identity = new Identity();
            identity.account = account;
            identity.name = "tea";
            identity.publicKey = "publicKey";
            identity.privateKey = "privateKey";

            return account.persist()
                    .replaceWith(identity.persist());
        });
    }

}
