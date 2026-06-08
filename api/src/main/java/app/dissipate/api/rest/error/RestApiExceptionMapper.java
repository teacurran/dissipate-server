package app.dissipate.api.rest.error;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Renders a thrown {@link RestApiException} as a localized {@link app.dissipate.api.rest.dto.ApiErrorResponse}
 * JSON envelope via {@link ApiErrorFactory}. Note: services that need transactional side-effects to
 * survive (audit rows, attempt counters) return an error {@code Response} directly instead of
 * throwing, since a failed reactive {@code Uni} rolls the transaction back.
 */
@Provider
public class RestApiExceptionMapper implements ExceptionMapper<RestApiException> {

  @Inject
  ApiErrorFactory apiErrorFactory;

  @Override
  public Response toResponse(RestApiException exception) {
    return apiErrorFactory.response(exception.getStatus(), exception.getCode(), exception.getArgs());
  }
}
