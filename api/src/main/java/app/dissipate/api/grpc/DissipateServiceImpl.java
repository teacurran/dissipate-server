package app.dissipate.api.grpc;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.jpa.converters.LocaleConverter;
import app.dissipate.data.models.Account;
import app.dissipate.data.models.AccountEmail;
import app.dissipate.data.models.Session;
import app.dissipate.data.models.SessionValidation;
import app.dissipate.exceptions.ApiException;
import app.dissipate.grpc.ApiError;
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
import app.dissipate.services.LocalizationService;
import app.dissipate.utils.EncryptionUtil;
import app.dissipate.utils.StringUtil;
import io.grpc.Status;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.mail.mailencoder.EmailAddress;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.Locale;
import java.util.ResourceBundle;

@GrpcService
@RegisterInterceptor(GrpcAuthInterceptor.class)
public class DissipateServiceImpl implements DissipateService {

  private static final Logger LOGGER = Logger.getLogger(DissipateServiceImpl.class);

  public static final String ERROR_EMAIL_INVALID = "auth.error.email.invalid";

  @Inject
  SnowflakeIdGenerator snowflakeIdGenerator;

  @Inject
  EncryptionUtil encryptionUtil;

  @Inject
  DelayedJobService delayedJobService;

  @Inject
  LocalizationService localizationService;

  @Override
  @WithSession
  @WithTransaction
  public Uni<RegisterResponse> register(RegisterRequest request) {
    Span otel = Span.current();

    String email = request.getEmail().toLowerCase();

    try (Scope scope = otel.makeCurrent()) {
      otel.setAttribute("email", email);
      otel.addEvent("register user", Attributes.of(AttributeKey.stringKey("request"), request.toString()));
      Locale locale = LocaleConverter.fromValue(request.getLocale());
      otel.setAttribute("locale", locale.toLanguageTag());

      ResourceBundle i18n = localizationService.getBundle(locale);

      return validateEmail(email).onItem().transformToUni(valid -> {
        if (!valid) {
          LOGGER.infov("invalid email: {0}", email);
          otel.addEvent("invalid email", Attributes.of(AttributeKey.stringKey("email"), email));
//          return Uni.createFrom().item(
//            RegisterResponse.newBuilder().setResult(RegisterResponseResult.Error)
//            .setError(ApiError.newBuilder()
//              .setCode(ERROR_EMAIL_INVALID)
//              .setMessage(i18n.getString(ERROR_EMAIL_INVALID))
//              .build()
//            ).build());
          throw new ApiException(Status.INVALID_ARGUMENT, ERROR_EMAIL_INVALID, i18n.getString(ERROR_EMAIL_INVALID));
        }

        return AccountEmail.findByEmailValidated(email)
          .onItem()
          .transformToUni(accountEmail -> {
            if (accountEmail != null) {
              LOGGER.infov("email already exists: {0}", email);
              otel.addEvent("email already exists", Attributes.of(AttributeKey.stringKey("email"), email));
              return Uni.createFrom().item(RegisterResponse.newBuilder().setResult(RegisterResponseResult.Error).build());
            }

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

  @WithSession
  public Uni<Boolean> validateEmail(String email) {
    String trimmedEmail = email.trim();
    if (trimmedEmail.isEmpty()) {
      return Uni.createFrom().item(false);
    }
    try {
      EmailAddress emailAddr = new EmailAddress(email);
      return Uni.createFrom().item(true);
    } catch (IllegalArgumentException e) {
      return Uni.createFrom().item(false);
    }
  }
}

