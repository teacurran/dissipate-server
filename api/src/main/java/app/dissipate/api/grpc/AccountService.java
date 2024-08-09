package app.dissipate.api.grpc;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.models.Account;
import app.dissipate.data.models.Account.AccountStatus;
import app.dissipate.data.models.AccountEmail;
import app.dissipate.grpc.CreateHandleRequest;
import app.dissipate.grpc.CreateHandleResponse;
import app.dissipate.grpc.DissipateService;
import app.dissipate.grpc.RegisterRequest;
import app.dissipate.grpc.RegisterResponse;
import app.dissipate.grpc.RegisterResponseResult;
import app.dissipate.interceptors.GrpcAuthInterceptor;
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
          account.status = AccountStatus.PENDING;
          return account.persistAndFlush().onItem().transformToUni(a -> {
            AccountEmail accountEmail1 = new AccountEmail();
            accountEmail1.account = a;
            accountEmail1.email = email;
            return accountEmail1.persistAndFlush().onItem().transformToUni(ae -> {
              return Uni.createFrom().item(RegisterResponse.newBuilder().setResult(RegisterResponseResult.EmailSent).build());
            });
          });
        }
      });
  }

  @Override
  @WithSession
  public Uni<CreateHandleResponse> createHandle(CreateHandleRequest request) {
    return Uni.createFrom().item(CreateHandleResponse.newBuilder().setHandle(request.getHandle()).build());
  }
}
