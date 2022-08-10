package app.dissipate.grpc;

import app.dissipate.interceptors.GrpcAuthInterceptor;
import app.dissipate.models.Account;
import app.dissipate.models.AccountStatus;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;

@GrpcService
@RegisterInterceptor(GrpcAuthInterceptor.class)
public class AccountService implements IAccountService {
    @Override
    public Uni<RegisterResponse> register(RegisterRequest request) {

//        return Panache.withTransaction(() -> {
//            Account account = new Account();
//            account.status = AccountStatus.ACTIVE;
//
//            account.persist().chain(
//        }
        return null;
    }

    @Override
    public Uni<Handle> createHandle(CreateHandleRequest request) {
        return null;
    }
}
