package app.dissipate.api.grpc;

import app.dissipate.data.models.Account;
import app.dissipate.data.models.AccountStatusEnum;
import app.dissipate.grpc.*;
import app.dissipate.interceptors.GrpcAuthInterceptor;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import static app.dissipate.constants.AuthenticationConstants.CONTEXT_FB_USER_KEY;

@GrpcService
@RegisterInterceptor(GrpcAuthInterceptor.class)
public class AccountService implements DissipateService {

    private static final Logger LOG = Logger.getLogger(AccountService.class);

    @Override
    public Uni<RegisterResponse> register(RegisterRequest request) {

        Span.current().addEvent("register user", Attributes.of(AttributeKey.stringKey("request"), request.toString()));

        LOG.info("Registering user: " + request);

        String token = CONTEXT_FB_USER_KEY.get();

        if (token == null) {
            return Uni.createFrom().nullItem();
        }

        return Panache.withTransaction(() -> {
            Account account = new Account();
            account.status = AccountStatusEnum.ACTIVE;
            account.srcId =

            account.persist().chain(
        }

        return Uni.createFrom().item(RegisterResponse.newBuilder()
                .setId(token)
                .build());

        //return null;
    }

    @Override
    public Uni<CreateHandleResponse> createHandle(CreateHandleRequest request) {
        return null;
    }
}
