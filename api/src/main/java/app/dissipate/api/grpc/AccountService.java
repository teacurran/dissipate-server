package app.dissipate.api.grpc;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.models.Account;
import app.dissipate.data.models.Account.AccountStatus;
import app.dissipate.data.models.AccountEmail;
import app.dissipate.data.models.Session;
import app.dissipate.data.models.SessionValidation;
import app.dissipate.grpc.CreateHandleRequest;
import app.dissipate.grpc.CreateHandleResponse;
import app.dissipate.grpc.DissipateService;
import app.dissipate.grpc.RegisterRequest;
import app.dissipate.grpc.RegisterResponse;
import app.dissipate.grpc.RegisterResponseResult;
import app.dissipate.interceptors.GrpcAuthInterceptor;
import app.dissipate.utils.StringUtil;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@GrpcService
@RegisterInterceptor(GrpcAuthInterceptor.class)
public class AccountService implements DissipateService {

  private static final Logger LOG = Logger.getLogger(AccountService.class);

  @Inject
  SnowflakeIdGenerator snowflakeIdGenerator;

  @Inject
  Tracer tracer;

  @Override
  @WithSession
  @WithTransaction
  public Uni<RegisterResponse> register(RegisterRequest request) {
    LOG.info("register()");
    Span currentSpan = Span.current();
    currentSpan.addEvent("register user", Attributes.of(AttributeKey.stringKey("request"), request.toString()));

    String email = request.getEmail().toLowerCase();

    return AccountEmail.findByEmailValidated(email)
      .onItem()
      .transformToUni(accountEmail -> {
        if (accountEmail != null) {
          LOG.infov("email already exists: {0}", email);
          currentSpan.addEvent("email already exists", Attributes.of(AttributeKey.stringKey("email"), email));
          return Uni.createFrom().item(RegisterResponse.newBuilder().setResult(RegisterResponseResult.Error).build());
        } else {
          Account account = new Account();
          account.id = snowflakeIdGenerator.generate(Account.ID_GENERATOR_KEY);
          account.status = AccountStatus.PENDING;
          return account.persistAndFlush().onItem().transformToUni(a -> {
            AccountEmail accountEmail2 = new AccountEmail();
            accountEmail2.id = snowflakeIdGenerator.generate(AccountEmail.ID_GENERATOR_KEY);
            accountEmail2.account = a;
            accountEmail2.email = email;
            return accountEmail2.persistAndFlush().onItem().transformToUni(ae -> {
              Session session = new Session();
              session.account = account;
              return session.persistAndFlush().onItem().transformToUni(s -> {
                SessionValidation sessionValidation = new SessionValidation();
                sessionValidation.session = s;
                sessionValidation.id = snowflakeIdGenerator.generate(SessionValidation.ID_GENERATOR_KEY);
                sessionValidation.email = ae;
                sessionValidation.token = StringUtil.generateRandomNumericString(6);
                return sessionValidation.persistAndFlush().onItem().transformToUni(sv -> {
                  return Uni.createFrom().item(RegisterResponse.newBuilder().setResult(RegisterResponseResult.EmailSent).build());
                });
              }).onFailure().call(t -> {
                LOG.error("error creating session", t);
                currentSpan.addEvent("error creating session", Attributes.of(AttributeKey.stringKey("error"), t.getMessage()));
                return Uni.createFrom().item(RegisterResponse.newBuilder().setResult(RegisterResponseResult.Error).build());
              });
            });
          });
        }
      }).onFailure().call(t -> {
        LOG.error("error registering user", t);
        currentSpan.addEvent("error registering user", Attributes.of(AttributeKey.stringKey("error"), t.getMessage()));
        return Uni.createFrom().item(RegisterResponse.newBuilder().setResult(RegisterResponseResult.Error).build());
      });
  }

  @Override
  @WithSession
  public Uni<CreateHandleResponse> createHandle(CreateHandleRequest request) {
    return Uni.createFrom().item(CreateHandleResponse.newBuilder().setHandle(request.getHandle()).build());
  }
}
