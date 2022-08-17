package app.dissipate;

import app.dissipate.models.Account;
import app.dissipate.models.AccountStatus;
import app.dissipate.models.Handle;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/users")
public class UserResource {

    @GET
    public Uni<Handle> hello() {
        return Panache.withTransaction(() -> {
            Account account = new Account();
            account.email = "tea@grilledcheese.com";
            account.status = AccountStatus.ACTIVE;

            Handle handle = new Handle();
            handle.account = account;
            handle.name = "tea";
            handle.publicKey = "publicKey";
            handle.privateKey = "privateKey";

            return account.persist()
                    .replaceWith(handle.persist());
        });
    }

}
