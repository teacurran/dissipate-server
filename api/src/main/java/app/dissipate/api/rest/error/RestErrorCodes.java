package app.dissipate.api.rest.error;

/**
 * i18n keys for REST error responses. These resolve against the {@code locales.i18n} resource
 * bundles via {@link app.dissipate.services.LocalizationService}. The REST equivalent of
 * {@link app.dissipate.api.grpc.GrpcErrorCodes}; auth keys are shared with the gRPC layer where
 * the semantics line up.
 */
public final class RestErrorCodes {

  /** No (or blank) {@code Authorization} header on an endpoint that requires one. */
  public static final String AUTH_REQUIRED = "error.auth.required";
  /** Bearer token did not resolve to a live, validated session. */
  public static final String AUTH_SESSION_INVALID = "error.auth.session.invalid";
  /** The session is authenticated but has no active identity selected. */
  public static final String AUTH_IDENTITY_REQUIRED = "error.auth.identity.required";
  /** Requested resource does not exist (or is not visible to the caller). */
  public static final String NOT_FOUND = "error.not_found";

  // Registration / OTP (auth.email.* keys are shared with the gRPC layer)
  /** Email address is missing or malformed. */
  public static final String AUTH_EMAIL_INVALID = "error.auth.email.invalid";
  /** Email is already claimed by a validated account. */
  public static final String AUTH_EMAIL_EXISTS = "error.auth.email.exists";
  /** OTP is wrong, expired, or has exhausted its attempts. */
  public static final String AUTH_OTP_INVALID = "error.auth.otp.invalid";
  /** Phone-based registration is not implemented yet. */
  public static final String AUTH_PHONE_UNSUPPORTED = "error.auth.phone.unsupported";

  // Password login
  /** Email/password combination did not match (generic, to avoid account enumeration). */
  public static final String AUTH_LOGIN_FAILED = "error.auth.login.failed";
  /** Too many failed attempts; the account is temporarily locked. */
  public static final String AUTH_ACCOUNT_LOCKED = "error.auth.account.locked";
  /** The supplied current password is incorrect (password change). */
  public static final String AUTH_PASSWORD_INVALID = "error.auth.password.invalid";

  /** Catch-all for unexpected server-side failures (no internals leaked to the client). */
  public static final String INTERNAL = "error.internal";

  private RestErrorCodes() {
    // class cannot be instantiated
  }
}
