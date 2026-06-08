package app.dissipate.api.rest.error;

import jakarta.ws.rs.core.Response;

/**
 * Application-level REST error. Carries an HTTP status plus an i18n {@code code} (a key from
 * {@link RestErrorCodes}); the localized human message is resolved at the edge by
 * {@link RestApiExceptionMapper} using the request locale, so a single thrown exception renders
 * correctly in any supported language.
 *
 * <p>This is the REST counterpart to the gRPC-native {@link app.dissipate.exceptions.ApiException}
 * (which extends {@code StatusRuntimeException} and cannot be reused over HTTP).
 */
public class RestApiException extends RuntimeException {

  private final transient Response.Status status;
  private final String code;
  private final transient Object[] args;

  public RestApiException(Response.Status status, String code, Object... args) {
    super(code);
    this.status = status;
    this.code = code;
    this.args = args;
  }

  public Response.Status getStatus() {
    return status;
  }

  public String getCode() {
    return code;
  }

  public Object[] getArgs() {
    return args;
  }
}
