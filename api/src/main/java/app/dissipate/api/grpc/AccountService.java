package app.dissipate.api.grpc;

import app.dissipate.data.models.Account;
import app.dissipate.data.models.Account.AccountStatus;
import app.dissipate.grpc.*;
import app.dissipate.interceptors.GrpcAuthInterceptor;
import com.google.firebase.auth.FirebaseToken;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import static app.dissipate.constants.AuthenticationConstants.CONTEXT_FB_USER_KEY;
import static app.dissipate.constants.AuthenticationConstants.CONTEXT_UID_KEY;

@GrpcService
@RegisterInterceptor(GrpcAuthInterceptor.class)
public class AccountService implements DissipateService {

    private static final Logger LOG = Logger.getLogger(AccountService.class);

    @Override
    public Uni<RegisterResponse> register(RegisterRequest request) {

        Span.current().addEvent("register user", Attributes.of(AttributeKey.stringKey("request"), request.toString()));

        LOG.info("Registering user: " + request);

        FirebaseToken token = CONTEXT_FB_USER_KEY.get();
        String uid = CONTEXT_UID_KEY.get();

        if (uid == null) {
            return Uni.createFrom().nullItem();
        }

        return Panache.withTransaction(() -> {
            Account account = new Account();
            account.status = AccountStatus.ACTIVE;
            account.srcId = uid;
            account.email = token.getEmail();

            return account.persist().replaceWith(Uni.createFrom().item(RegisterResponse.newBuilder()
                    .setId(uid)
                    .build()));
        });
    }

    @Override
    public Uni<CreateHandleResponse> createHandle(CreateHandleRequest request) {
        return null;
    }
}
