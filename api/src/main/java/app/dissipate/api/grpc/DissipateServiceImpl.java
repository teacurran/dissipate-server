package app.dissipate.api.grpc;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.jpa.converters.LocaleConverter;
import app.dissipate.data.models.Account;
import app.dissipate.data.models.AccountEmail;
import app.dissipate.data.models.Session;
import app.dissipate.data.models.SessionValidation;
import app.dissipate.grpc.CreateHandleRequest;
import app.dissipate.grpc.CreateHandleResponse;
import app.dissipate.grpc.DissipateService;
import app.dissipate.grpc.RegisterRequest;
import app.dissipate.grpc.RegisterResponse;
import app.dissipate.grpc.RegisterResponseResult;
import app.dissipate.grpc.ValidateSessionRequest;
import app.dissipate.grpc.ValidateSessionResponse;
import app.dissipate.interceptors.GrpcAuthInterceptor;
import app.dissipate.services.DelayedJobService;
import app.dissipate.services.MessagingService;
import app.dissipate.utils.EncryptionUtil;
import app.dissipate.utils.StringUtil;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.Locale;

@GrpcService
@RegisterInterceptor(GrpcAuthInterceptor.class)
public class DissipateServiceImpl implements DissipateService {

  private static final Logger LOGGER = Logger.getLogger(DissipateServiceImpl.class);

  @Inject
  SnowflakeIdGenerator snowflakeIdGenerator;

  @Inject
  MessagingService messagingService;

  @Inject
  EncryptionUtil encryptionUtil;

  @Inject
  DelayedJobService delayedJobService;

  @Override
  @WithSession
  @WithTransaction
  public Uni<RegisterResponse> register(RegisterRequest request) {
    Span otel = Span.current();

    String email = request.getEmail().toLowerCase();

    try (Scope scope = otel.makeCurrent()) {
      otel.setAttribute("email", email);
      otel.addEvent("register user", Attributes.of(AttributeKey.stringKey("request"), request.toString()));

      return AccountEmail.findByEmailValidated(email)
        .onItem()
        .transformToUni(accountEmail -> {
          if (accountEmail != null) {
            LOGGER.infov("email already exists: {0}", email);
            otel.addEvent("email already exists", Attributes.of(AttributeKey.stringKey("email"), email));
            return Uni.createFrom().item(RegisterResponse.newBuilder().setResult(RegisterResponseResult.Error).build());
          }
          Locale locale = LocaleConverter.fromValue(request.getLocale());
          otel.setAttribute("locale", locale.toLanguageTag());
          return Account.createNewAnonymousAccount(locale, email, snowflakeIdGenerator, encryptionUtil).onItem().transformToUni(a -> {
            Session session = new Session();
            session.account = a;
            return session.persistAndFlush().onItem().transformToUni(s -> {
              SessionValidation sessionValidation = new SessionValidation();
              sessionValidation.session = s;
              sessionValidation.id = snowflakeIdGenerator.generate(SessionValidation.ID_GENERATOR_KEY);
              sessionValidation.email = a.emails.get(0);
              sessionValidation.token = StringUtil.generateRandomString(6);
              return sessionValidation.persistAndFlush().onItem().transformToUni(sv -> {
                return delayedJobService.createDelayedJob(sv).onItem().transformToUni(dj -> {
                  return Uni.createFrom().item(
                    RegisterResponse.newBuilder()
                      .setResult(RegisterResponseResult.EmailSent)
                      .setSid(s.id.toString())
                      .build()

                  );
                });
              });
            }).onFailure().call(t -> {
              otel.addEvent("error creating session", Attributes.of(AttributeKey.stringKey("error"), t.getMessage()));
              return Uni.createFrom().item(RegisterResponse.newBuilder().setResult(RegisterResponseResult.Error).build());
            });
          });

        }).onFailure().call(t -> {
          LOGGER.error("error registering user", t);
          otel.addEvent("error registering user", Attributes.of(AttributeKey.stringKey("error"), t.getMessage()));
          return Uni.createFrom().item(RegisterResponse.newBuilder().setResult(RegisterResponseResult.Error).build());
        });
    }
  }

  @Override
  @WithSession
  public Uni<CreateHandleResponse> createHandle(CreateHandleRequest request) {
    return Uni.createFrom().item(CreateHandleResponse.newBuilder().setHandle(request.getHandle()).build());
  }

  @Override
  @WithSession
  @WithTransaction
  @WithSpan("DissipateServiceImpl.validateSession")
  public Uni<ValidateSessionResponse> validateSession(ValidateSessionRequest request) {
    return SessionValidation.byId(request.getSid()).onItem().transformToUni(sv -> {
      if (sv == null) {
        return Uni.createFrom().item(ValidateSessionResponse.newBuilder().setValid(false).build());
      }
      if (sv.token.equals(request.getOtp())) {
        sv.validated = Instant.now();
        return sv.session.persistAndFlush().onItem().transformToUni(s -> {
          return Uni.createFrom().item(ValidateSessionResponse.newBuilder().setValid(true).build());
        });
      }
      return Uni.createFrom().item(ValidateSessionResponse.newBuilder().setValid(false).build());
    });
  }
}
