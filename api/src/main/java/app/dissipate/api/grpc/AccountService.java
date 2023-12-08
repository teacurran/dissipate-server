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
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import java.time.LocalDateTime;

@GrpcService
@RegisterInterceptor(GrpcAuthInterceptor.class)
public class AccountService implements DissipateService {

    private static final Logger LOG = Logger.getLogger(AccountService.class);

    @Inject
    SnowflakeIdGenerator snowflakeIdGenerator;

    @Inject
    CurrentIdentityAssociation identityAssociation;

    @Inject
    Tracer tracer;


    @Override
    @WithSession
    @WithTransaction
    public Uni<RegisterResponse> register(RegisterRequest request) {
        LOG.info("register()");
        Span currentSpan = Span.current();
        currentSpan.addEvent("register user", Attributes.of(AttributeKey.stringKey("request"), request.toString()));

        return identityAssociation.getDeferredIdentity().chain(identity -> {
            identity.getAttributes().forEach((k, v) -> {
                LOG.infov("identity attribute: {0}={1}", k, v);

                String identityAttributeKey = "identity." + k;
                currentSpan.setAttribute(AttributeKey.stringKey(identityAttributeKey), v.toString());
            });

            LOG.infov("identity: {0}", identity.toString());
            currentSpan.addEvent("identity", Attributes.of(AttributeKey.stringKey("identity"), identity.toString()));

            FirebaseTokenVO token = identity.getAttribute("fb_token");

            //FirebaseTokenVO token = CONTEXT_FB_USER_KEY.get();
            String uid = token.getUid();

            LOG.infov("register user with uid: {0}", uid);

            if (uid == null) {
                LOG.info("uid is null");
                currentSpan.addEvent("uid is null");
                return Uni.createFrom().nullItem();
            }
            return Panache.withTransaction(() -> Account.findBySrcId(uid).onItem().ifNotNull().transformToUni(account -> {
                LOG.infov("account exists: {0}", account.id);
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
                return RegisterResponse.newBuilder().setId(uid).build();
            }));
        });
    }

    @Override
    @WithSession
    public Uni<CreateHandleResponse> createHandle(CreateHandleRequest request) {
        return Uni.createFrom().item(CreateHandleResponse.newBuilder().setHandle(request.getHandle()).build());
    }
}
