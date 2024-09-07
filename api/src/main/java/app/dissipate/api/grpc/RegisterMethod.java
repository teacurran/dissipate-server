package app.dissipate.api.grpc;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.models.Account;
import app.dissipate.data.models.AccountEmail;
import app.dissipate.data.models.Session;
import app.dissipate.data.models.SessionValidation;
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
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.mail.mailencoder.EmailAddress;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Locale;

import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_EMAIL_INVALID;
import static io.opentelemetry.semconv.ExceptionAttributes.EXCEPTION_ESCAPED;

@ApplicationScoped
public class RegisterMethod {

  private static final Logger LOGGER = Logger.getLogger(RegisterMethod.class);

  @Inject
  DelayedJobService delayedJobService;

  @Inject
  LocalizationService localizationService;

  @Inject
  SnowflakeIdGenerator snowflakeIdGenerator;

  @Inject
  EncryptionUtil encryptionUtil;


  @WithSpan("RegisterMethod.register")
  public Uni<RegisterResponse> register(RegisterRequest request) {
    Span otel = Span.current();
    Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();

    otel.addEvent("register user", Attributes.of(AttributeKey.stringKey("request"), request.toString()));

    return validateEmail(request.getEmail()).onItem().transformToUni(email ->
      AccountEmail.findByEmailValidated(email).onItem().transformToUni(accountEmail -> {
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
            return sessionValidation.persistAndFlush()
              .onItem().transformToUni(sv -> delayedJobService.createDelayedJob(sv)
                .onItem().transformToUni(dj -> Uni.createFrom().item(
                  RegisterResponse.newBuilder().setResult(RegisterResponseResult.EmailSent).setSid(s.id.toString()).build()
                ))
              );
          });
        });
      })
    ).onFailure().call(t -> {
      otel.addEvent("error registering user", Attributes.of(AttributeKey.stringKey("error"), t.getMessage()));
      otel.recordException(t, Attributes.of(EXCEPTION_ESCAPED, true));
      return Uni.createFrom().item(RegisterResponse.newBuilder().setResult(RegisterResponseResult.Error).build());
    });
  }

  @WithSession
  public Uni<String> validateEmail(String email) {
    Span otel = Span.current();
    otel.setAttribute("email", email);
    Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();
    String formattedEmail = email.toLowerCase().trim();
    if (formattedEmail.isEmpty()) {
      return Uni.createFrom().failure(localizationService.getApiException(locale, Status.INVALID_ARGUMENT, AUTH_EMAIL_INVALID));
    }
    try {
      new EmailAddress(formattedEmail);
    } catch (IllegalArgumentException e) {
      return Uni.createFrom().failure(localizationService.getApiException(locale, Status.INVALID_ARGUMENT, AUTH_EMAIL_INVALID));
    }
    return Uni.createFrom().item(formattedEmail);
  }

}
