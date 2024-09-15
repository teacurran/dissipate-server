package app.dissipate.api.grpc;

import app.dissipate.data.models.AccountEmail;
import app.dissipate.data.models.AccountStatus;
import app.dissipate.data.models.SessionValidation;
import app.dissipate.grpc.ValidateSessionRequest;
import app.dissipate.grpc.ValidateSessionResponse;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import app.dissipate.services.LocalizationService;
import app.dissipate.utils.EncryptionUtil;
import io.grpc.Status;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.Locale;

import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_TOKEN_INVALID;

@ApplicationScoped
public class ValidateSessionMethod {

  private static final Logger LOGGER = Logger.getLogger(ValidateSessionMethod.class);

  @Inject
  LocalizationService localizationService;

  @Inject
  EncryptionUtil encryptionUtil;

  @WithSpan("ValidateSessionMethod")
  public Uni<ValidateSessionResponse> validateSession(ValidateSessionRequest request) {
    Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();

    String sid = request.getSid().trim();
    String otp = request.getOtp().trim();

    if (sid.isEmpty() || otp.isEmpty()) {
      return Uni.createFrom().failure(localizationService.getApiException(locale, Status.NOT_FOUND, AUTH_TOKEN_INVALID));
    }

    return SessionValidation.findBySidToken(sid, otp).onItem().transformToUni(sv -> {
      if (sv == null) {
        return Uni.createFrom().failure(localizationService.getApiException(locale, Status.NOT_FOUND, AUTH_TOKEN_INVALID));
      }
      if (sv.validated != null) {
        return Uni.createFrom().failure(localizationService.getApiException(locale, Status.NOT_FOUND, AUTH_TOKEN_INVALID));
      }

      String email = null;
      if (sv.email != null) {
        email = sv.email.email;
      }

      return checkForExistingValidatedEmail(email)
        .onItem().transformToUni(v -> markEmailValidated(sv))
        .onItem().transformToUni(v -> {
          sv.validated = Instant.now();
          return sv.session.persistAndFlush()
            .onItem().transformToUni(s -> Uni.createFrom().item(
              ValidateSessionResponse.newBuilder().setValid(true).build()
            ));
        });
    });
  }

  public Uni<Void> markEmailValidated(SessionValidation sv) {
    if (sv.email == null) {
      return Uni.createFrom().voidItem();
    }
    return sv.email.markValidated().onItem().transform(accountEmail -> {
      // if account was anonymous, mark it as active
      if (accountEmail.account != null && AccountStatus.ANONYMOUS.equals(accountEmail.account.status)) {
        accountEmail.account.status = AccountStatus.ACTIVE;
        return accountEmail.account.persistAndFlush(encryptionUtil);
      }
      return Uni.createFrom().voidItem();
    }).onItem().transformToUni(v -> Uni.createFrom().voidItem());
  }

  public Uni<Void> checkForExistingValidatedEmail(String email) {
    LOGGER.debug("checkForExistingValidatedEmail: " + email);
    if (email == null) {
      LOGGER.debug("email is null");
      return Uni.createFrom().voidItem();
    }
    Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();
    return AccountEmail.findByEmailValidated(email).onItem().transformToUni(accountEmail -> {
      LOGGER.debug("accountEmail: " + accountEmail);
      if (accountEmail != null) {
        return Uni.createFrom().failure(localizationService.getApiException(locale, Status.NOT_FOUND, AUTH_TOKEN_INVALID));
      }
      return Uni.createFrom().voidItem();
    });
  }

}


