package app.dissipate.api.grpc;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class GrpcErrorCodes {

  public static final String AUTH_EMAIL_INVALID  = "error.auth.email.invalid";
  public static final String AUTH_EMAIL_EXISTS  = "error.auth.email.exists";
  public static final String AUTH_TOKEN_CONSUMED = "error.auth.token.consumed";
  public static final String AUTH_TOKEN_INVALID  = "error.auth.token.invalid";
  public static final String AUTH_REQUIRED       = "error.auth.required";
  public static final String AUTH_SESSION_INVALID = "error.auth.session.invalid";
  public static final String AUTH_FORBIDDEN      = "error.auth.forbidden";
  public static final String AUTH_LOGIN_FAILED   = "error.auth.login.failed";
  public static final String AUTH_ACCOUNT_LOCKED = "error.auth.account.locked";
  public static final String OAUTH_INVALID_CLIENT = "error.oauth.invalid_client";

  private GrpcErrorCodes() {
    // class cannot be instantiated
  }
}
