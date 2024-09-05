package app.dissipate.exceptions;

public class DissipateRuntimeException extends RuntimeException {
  public DissipateRuntimeException(String message) {
    super(message);
  }

  public DissipateRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }
}
