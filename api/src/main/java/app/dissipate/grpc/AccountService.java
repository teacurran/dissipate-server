package app.dissipate.grpc;

import app.dissipate.interceptors.GrpcAuthInterceptor;
import app.dissipate.models.AccountStatus;
import com.google.firebase.auth.FirebaseToken;
import io.grpc.Context;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;

import static app.dissipate.constants.AuthenticationConstants.CONTEXT_FB_USER_KEY;

@GrpcService
@RegisterInterceptor(GrpcAuthInterceptor.class)
public class AccountService implements IAccountService {

    @Override
    public Uni<RegisterResponse> register(RegisterRequest request) {
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
