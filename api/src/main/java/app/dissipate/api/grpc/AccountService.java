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
import io.opentelemetry.api.trace.Tracer;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.opentelemetry.runtime.tracing.intrumentation.grpc.GrpcTracingServerInterceptor;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import java.time.LocalDateTime;

import static app.dissipate.constants.AuthenticationConstants.CONTEXT_FB_USER_KEY;
import static app.dissipate.constants.AuthenticationConstants.CONTEXT_UID_KEY;

@GrpcService
@RegisterInterceptor(GrpcAuthInterceptor.class)
public class AccountService implements DissipateService {

    private static final Logger LOG = Logger.getLogger(AccountService.class);

    @Inject
    SnowflakeIdGenerator snowflakeIdGenerator;

    @Inject
    Tracer tracer;

    @Override
    public Uni<RegisterResponse> register(RegisterRequest request) {

        Span currentSpan = Span.current();
        currentSpan.addEvent("register user", Attributes.of(AttributeKey.stringKey("request"), request.toString()));

        FirebaseTokenVO token = CONTEXT_FB_USER_KEY.get();
        String uid = CONTEXT_UID_KEY.get();
        LOG.debugv("register user with uid: {0}", uid);

        if (uid == null) {
            currentSpan.addEvent("uid is null");
            return Uni.createFrom().nullItem();
        }

        return Panache.withTransaction(() -> Account.findBySrcId(uid).onItem().ifNotNull().transformToUni(account -> {
            LOG.debugv("account exists: {0}", account.id);
            currentSpan.addEvent("account exists", Attributes.of(AttributeKey.longKey("account.id"), account.id));
            account.email = token.getEmail();
            account.status = AccountStatus.ACTIVE;
            account.updatedAt = LocalDateTime.now();

            return account.persistAndFlush();
        }).onItem().ifNull().switchTo(() -> {
            Account account = new Account();
            account.id = snowflakeIdGenerator.generate(Account.class.getName());
            account.email = token.getEmail();
            account.status = AccountStatus.ACTIVE;
            account.srcId = uid;
            LOG.debugv("new account: {0}", account.id);
            currentSpan.addEvent("new account", Attributes.of(AttributeKey.longKey("account.id"), account.id));
            return account.persistAndFlush();
        }).onItem().transform(account -> {
            LOG.debugv("using account: {0}", account.id);
            //currentSpan.addEvent("account created", Attributes.of(AttributeKey.longKey("account.id"), account.id));
            RegisterResponse response = RegisterResponse.newBuilder().setId(uid).build();
            return response;
        }));
    }

    @Override
    public Uni<CreateHandleResponse> createHandle(CreateHandleRequest request) {
        return null;
    }
}
