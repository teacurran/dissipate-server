package app.dissipate.exceptions;

public class DelayedJobException extends Exception {
  private final boolean isFatal;
  private final String message;

  public DelayedJobException(boolean isFatal, String message) {
    super(message);
    this.isFatal = isFatal;
    this.message = message;
  }

  public boolean isFatal() {
    return isFatal;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
