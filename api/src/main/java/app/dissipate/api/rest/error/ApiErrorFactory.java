package app.dissipate.api.rest.error;

import app.dissipate.api.rest.dto.ApiErrorResponse;
import app.dissipate.api.rest.i18n.RequestLocale;
import app.dissipate.services.LocalizationService;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Builds the localized {@link ApiErrorResponse} envelope. Used by {@link RestApiExceptionMapper} for
 * thrown exceptions and by services that need to return an error as a committed {@link Response}
 * value (so transactional side-effects — audit rows, attempt counters — are not rolled back).
 */
@ApplicationScoped
public class ApiErrorFactory {

  @Inject
  RequestLocale requestLocale;

  @Inject
  LocalizationService localizationService;

  public ApiErrorResponse body(String code, Object... args) {
    String message = localizationService.getMessage(requestLocale.getLocale(), code, args);
    SpanContext spanContext = Span.current().getSpanContext();
    String traceId = spanContext.isValid() ? spanContext.getTraceId() : null;
    return new ApiErrorResponse(code, message, traceId);
  }

  public Response response(Response.Status status, String code, Object... args) {
    return response(status.getStatusCode(), code, args);
  }

  /** Variant for status codes without a {@link Response.Status} constant (e.g. 429). */
  public Response response(int status, String code, Object... args) {
    return Response.status(status)
      .type(MediaType.APPLICATION_JSON)
      .entity(body(code, args))
      .build();
  }
}
