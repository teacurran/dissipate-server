package app.dissipate.exceptions;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class ApiException extends StatusRuntimeException {
  private final String code;
  private final String message;

  public ApiException(Status status, String code, String message) {
    super(status.withDescription(code), getTrailers(code, message));
    this.code = code;
    this.message = message;
  }

  public static Metadata getTrailers(String code, String message) {
    Metadata trailers = new Metadata();
    trailers.put(Metadata.Key.of("code", Metadata.ASCII_STRING_MARSHALLER), code);
    trailers.put(Metadata.Key.of("message", Metadata.ASCII_STRING_MARSHALLER), message);
    return trailers;
  }

  public String getCode() {
    return code;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
