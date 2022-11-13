package app.dissipate.api.grpc;

import app.dissipate.beans.FirebaseTokenVO;
import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.models.Account;
import app.dissipate.data.models.Account.AccountStatus;
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

import javax.inject.Inject;

import static app.dissipate.constants.AuthenticationConstants.CONTEXT_FB_USER_KEY;
import static app.dissipate.constants.AuthenticationConstants.CONTEXT_UID_KEY;

@GrpcService
@RegisterInterceptor(GrpcAuthInterceptor.class)
public class AccountService implements DissipateService {

    private static final Logger LOG = Logger.getLogger(AccountService.class);

    @Inject
    SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public Uni<RegisterResponse> register(RegisterRequest request) {

        Span.current().addEvent("register user", Attributes.of(AttributeKey.stringKey("request"), request.toString()));

        LOG.info("Registering user: " + request);

        FirebaseTokenVO token = CONTEXT_FB_USER_KEY.get();
        String uid = CONTEXT_UID_KEY.get();

        if (uid == null) {
            LOG.info("uid is null");
            return Uni.createFrom().nullItem();
        }

        return Panache.withTransaction(() -> Account.findBySrcId(uid)
                .onItem().ifNotNull().transform(account -> {
                    LOG.info("Account already exists: " + account);

                    return Uni.createFrom().item(account);
                }).onItem().ifNull().continueWith(() -> {
                    LOG.info("creating account with srcId: " + uid);
                    Account account = new Account();
                    account.id = snowflakeIdGenerator.generate(Account.class.getName());
                    account.email = token.getEmail();
                    account.status = AccountStatus.ACTIVE;
                    account.srcId = uid;
                    return account.persist();
                }).onItem().transform(account -> {
                    LOG.info("Account created: " + uid);
                    return RegisterResponse.newBuilder().setId(uid).build();
                }));

    }

    @Override
    public Uni<CreateHandleResponse> createHandle(CreateHandleRequest request) {
        return null;
    }
}
