package app.dissipate.services.grpc;

import app.dissipate.grpc.*;
import app.dissipate.interceptors.GrpcAuthInterceptor;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import static app.dissipate.constants.AuthenticationConstants.CONTEXT_FB_USER_KEY;

@GrpcService
@RegisterInterceptor(GrpcAuthInterceptor.class)
public class AccountService implements DissipateService {

    private static final Logger LOG = Logger.getLogger(AccountService.class);

    @Override
    public Uni<RegisterResponse> register(RegisterRequest request) {
        LOG.info("Registering user: " + request);

        String token = CONTEXT_FB_USER_KEY.get();

        if (token == null) {
            return Uni.createFrom().nullItem();
        }

        return Uni.createFrom().item(RegisterResponse.newBuilder()
                .setAccount(Account.newBuilder().setId(token))
                .build());

//        return Panache.withTransaction(() -> {
//            Account account = new Account();
//            account.status = AccountStatus.ACTIVE;
//
//            account.persist().chain(
//        }
        //return null;
    }

    @Override
    public Uni<Handle> createHandle(CreateHandleRequest request) {
        return null;
    }
}
