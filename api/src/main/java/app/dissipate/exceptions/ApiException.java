package app.dissipate.exceptions;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class ApiException extends StatusRuntimeException {
  private final String code;
  private final String message;

  public ApiException(Status status, String code, String message) {
    super(status.withDescription(message), getTrailers(code));
    this.code = code;
    this.message = message;
  }

  public static Metadata getTrailers(String code) {
    Metadata trailers = new Metadata();
    trailers.put(Metadata.Key.of("code", Metadata.ASCII_STRING_MARSHALLER), code);
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
