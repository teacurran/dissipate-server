package app.dissipate.api.grpc;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class GrpcErrorCodes {

  public static final String AUTH_EMAIL_INVALID  = "error.auth.email.invalid";
  public static final String AUTH_EMAIL_EXISTS  = "error.auth.email.exists";
  public static final String AUTH_TOKEN_CONSUMED = "error.auth.token.consumed";
  public static final String AUTH_TOKEN_INVALID  = "error.auth.token.invalid";

  private GrpcErrorCodes() {
    // class cannot be instantiated
  }
}
