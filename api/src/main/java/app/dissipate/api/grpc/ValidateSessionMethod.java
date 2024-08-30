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
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.Locale;

import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_TOKEN_INVALID;

@ApplicationScoped
public class ValidateSessionMethod {

  @Inject
  LocalizationService localizationService;

  public Uni<ValidateSessionResponse> validateSession(ValidateSessionRequest request) {
    Span otel = Span.current();

    Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();

    // This is in progress, trying to use protovalidate, it isn't working yet
    //    try {
    //      Validator validator = new Validator();
    //      validator.validate(request);
    //    } catch (ValidationException e) {
    //      return Uni.createFrom().item(ValidateSessionResponse.newBuilder().setValid(false).build());
    //    }

    otel.setAttribute("locale", locale.toLanguageTag());

    return SessionValidation.findBySidToken(request.getSid(), request.getOtp()).onItem().transformToUni(sv -> {
      if (sv == null) {
        throw localizationService.getApiException(locale, Status.NOT_FOUND, AUTH_TOKEN_INVALID);
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

