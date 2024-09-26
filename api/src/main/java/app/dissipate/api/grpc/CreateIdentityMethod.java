package app.dissipate.api.grpc;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.models.Account;
import app.dissipate.data.models.AccountEmail;
import app.dissipate.data.models.Identity;
import app.dissipate.data.models.Session;
import app.dissipate.data.models.SessionValidation;
import app.dissipate.grpc.CreateIdentityRequest;
import app.dissipate.grpc.CreateIdentityResponse;
import app.dissipate.grpc.GetSessionResponse;
import app.dissipate.grpc.RegisterRequest;
import app.dissipate.grpc.RegisterResponse;
import app.dissipate.grpc.RegisterResponseResult;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import app.dissipate.interceptors.GrpcSecurityInterceptor;
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
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.mail.mailencoder.EmailAddress;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Locale;

import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_EMAIL_INVALID;
import static io.opentelemetry.semconv.ExceptionAttributes.EXCEPTION_ESCAPED;

@ApplicationScoped
public class CreateIdentityMethod {

  private static final Logger LOGGER = Logger.getLogger(CreateIdentityMethod.class);

  @Inject
  DelayedJobService delayedJobService;

  @Inject
  LocalizationService localizationService;

  @Inject
  SnowflakeIdGenerator snowflakeIdGenerator;

  @Inject
  EncryptionUtil encryptionUtil;

  @Inject
  CurrentIdentityAssociation identity;


  @WithSpan("RegisterMethod.register")
  public Uni<CreateIdentityResponse> create(CreateIdentityRequest request) {
    Span otel = Span.current();
    Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();
    otel.addEvent("register user", Attributes.of(AttributeKey.stringKey("request"), request.toString()));

    return identity.getDeferredIdentity().onItem().transformToUni(si -> {
      Session session = si.getAttribute("session");

      if (session == null || session.account == null) {
        return Uni.createFrom().failure(localizationService.getApiException(locale, Status.PERMISSION_DENIED, AUTH_EMAIL_INVALID));
      }

      Identity identity = new Identity();
      identity.id = snowflakeIdGenerator.generate(Identity.ID_GENERATOR_KEY);
      identity.account = session.account;
      identity.username = request.getUsername();
      identity.name = request.getName();
      return identity.persistAndFlush(encryptionUtil)
        .onItem().transform(i -> CreateIdentityResponse.newBuilder()
          .setIid(i.id.toString())
          .setSid(session.id.toString())
          .setUsername(i.username)
          .setName(i.name).build());
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
