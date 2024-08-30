package app.dissipate.api.grpc;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.models.Account;
import app.dissipate.data.models.AccountEmail;
import app.dissipate.data.models.Session;
import app.dissipate.data.models.SessionValidation;
import app.dissipate.exceptions.ApiException;
import app.dissipate.grpc.RegisterRequest;
import app.dissipate.grpc.RegisterResponse;
import app.dissipate.grpc.RegisterResponseResult;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import app.dissipate.services.DelayedJobService;
import app.dissipate.services.LocalizationService;
import app.dissipate.utils.EncryptionUtil;
import app.dissipate.utils.StringUtil;
import io.grpc.Status;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.mail.mailencoder.EmailAddress;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Locale;
import java.util.ResourceBundle;

import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_EMAIL_INVALID;
import static io.opentelemetry.semconv.ExceptionAttributes.EXCEPTION_ESCAPED;

@ApplicationScoped
public class RegisterMethod {

  private static final Logger LOGGER = Logger.getLogger(DissipateServiceImpl.class);

  @Inject
  DelayedJobService delayedJobService;

  @Inject
  LocalizationService localizationService;

  @Inject
  SnowflakeIdGenerator snowflakeIdGenerator;

  @Inject
  EncryptionUtil encryptionUtil;


  public Uni<RegisterResponse> register(RegisterRequest request) {
    Span otel = Span.current();
    Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();

    String email = request.getEmail().toLowerCase();

    otel.setAttribute("email", email);
    otel.addEvent("register user", Attributes.of(AttributeKey.stringKey("request"), request.toString()));
    otel.setAttribute("locale", locale.toLanguageTag());
    ResourceBundle i18n = localizationService.getBundle(locale);

    return validateEmail(email).onItem().transformToUni(valid -> {
      if (!valid) {
        LOGGER.infov("invalid email: {0}", email);
        otel.addEvent("invalid email", Attributes.of(AttributeKey.stringKey("email"), email));
        throw new ApiException(Status.INVALID_ARGUMENT, AUTH_EMAIL_INVALID, i18n.getString(AUTH_EMAIL_INVALID));
      }

      return AccountEmail.findByEmailValidated(email).onItem().transformToUni(accountEmail -> {
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
                return Uni.createFrom().item(RegisterResponse.newBuilder().setResult(RegisterResponseResult.EmailSent).setSid(s.id.toString()).build()
                );
              });
            });
          }).onFailure().call(t -> {
            otel.recordException(t, Attributes.of(EXCEPTION_ESCAPED, true));
            return Uni.createFrom().item(RegisterResponse.newBuilder().setResult(RegisterResponseResult.Error).build());
          });
        });
      });

    }).onFailure().call(t -> {
      otel.addEvent("error registering user", Attributes.of(AttributeKey.stringKey("error"), t.getMessage()));
      otel.recordException(t, Attributes.of(EXCEPTION_ESCAPED, true));
      return Uni.createFrom().item(RegisterResponse.newBuilder().setResult(RegisterResponseResult.Error).build());
    });
  }

  @WithSession
  public Uni<Boolean> validateEmail(String email) {
    String trimmedEmail = email.trim();
    if (trimmedEmail.isEmpty()) {
      return Uni.createFrom().item(false);
    }
    try {
      new EmailAddress(email);
      return Uni.createFrom().item(true);
    } catch (IllegalArgumentException e) {
      return Uni.createFrom().item(false);
    }
  }

}
