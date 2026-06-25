package app.dissipate.constants;

import app.dissipate.grpc.v1.MethodPolicy;
import io.grpc.Context;
import io.grpc.Metadata;

public class AuthenticationConstants {

  public static final Metadata.Key<String> AUTH_HEADER_KEY = Metadata.Key.of("Authentication", Metadata.ASCII_STRING_MARSHALLER);

  /** Standard bearer-token header carrying a validated session id (the "sid"). Case-insensitive. */
  public static final Metadata.Key<String> AUTHORIZATION_HEADER_KEY = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

  /** Scheme prefix expected on {@link #AUTHORIZATION_HEADER_KEY}, e.g. {@code "Bearer <sid>"}. */
  public static final String BEARER_PREFIX = "Bearer ";

  public static final Context.Key<String> CONTEXT_UID_KEY = Context.key("uid");

  /** The resolved {@link MethodPolicy} for the in-flight call, stashed by the authn interceptor. */
  public static final Context.Key<MethodPolicy> POLICY_KEY = Context.key("method-policy");

  /** The raw bearer token extracted from call metadata, stashed by the authn interceptor. */
  public static final Context.Key<String> BEARER_TOKEN_KEY = Context.key("bearer-token");

  private AuthenticationConstants() {
    // class cannot be instantiated
  }
}
