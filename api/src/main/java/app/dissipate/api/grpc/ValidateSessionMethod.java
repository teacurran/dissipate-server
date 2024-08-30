package app.dissipate.api.grpc;

import app.dissipate.data.jpa.converters.LocaleConverter;
import app.dissipate.data.models.SessionValidation;
import app.dissipate.grpc.ValidateSessionRequest;
import app.dissipate.grpc.ValidateSessionResponse;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import app.dissipate.services.LocalizationService;
import io.grpc.Context;
import io.grpc.Status;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.mail.mailencoder.EmailAddress;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.Locale;

import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_TOKEN_INVALID;

@ApplicationScoped
public class ValidateSessionMethod {

  @Inject
  LocalizationService localizationService;

  @WithSpan("ValidateSessionMethod")
  public Uni<ValidateSessionResponse> validateSession(ValidateSessionRequest request) {
    Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();

    String sid = request.getSid().trim();
    String otp = request.getOtp().trim();

    if (sid.isEmpty() || otp.isEmpty()) {
      return Uni.createFrom().failure(localizationService.getApiException(locale, Status.INVALID_ARGUMENT, AUTH_TOKEN_INVALID));
    }

    return SessionValidation.findBySidToken(sid, otp).onItem().transformToUni(sv -> {
      if (sv == null) {
        return Uni.createFrom().failure(localizationService.getApiException(locale, Status.NOT_FOUND, AUTH_TOKEN_INVALID));
      }
      if (sv.token.equals(otp)) {
        sv.validated = Instant.now();
        return sv.session.persistAndFlush().onItem().transformToUni(s -> {
          return Uni.createFrom().item(ValidateSessionResponse.newBuilder().setValid(true).build());
        });
      }
      return Uni.createFrom().item(ValidateSessionResponse.newBuilder().setValid(false).build());
    });
  }
}

