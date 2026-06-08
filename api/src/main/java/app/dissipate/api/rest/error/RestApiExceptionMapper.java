package app.dissipate.api.rest.error;

import app.dissipate.api.rest.dto.ApiErrorResponse;
import app.dissipate.api.rest.i18n.RequestLocale;
import app.dissipate.services.LocalizationService;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Renders {@link RestApiException} as a localized {@link ApiErrorResponse} JSON envelope, using the
 * request locale resolved by {@code RestLocaleFilter} and the current OpenTelemetry trace id.
 */
@Provider
public class RestApiExceptionMapper implements ExceptionMapper<RestApiException> {

  @Inject
  RequestLocale requestLocale;

  @Inject
  LocalizationService localizationService;

  @Override
  public Response toResponse(RestApiException exception) {
    String message = localizationService.getMessage(
      requestLocale.getLocale(), exception.getCode(), exception.getArgs());

    SpanContext spanContext = Span.current().getSpanContext();
    String traceId = spanContext.isValid() ? spanContext.getTraceId() : null;

    ApiErrorResponse body = new ApiErrorResponse(exception.getCode(), message, traceId);

    return Response.status(exception.getStatus())
      .type(MediaType.APPLICATION_JSON)
      .entity(body)
      .build();
  }
}
