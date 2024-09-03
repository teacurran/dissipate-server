package app.dissipate.constants;

import app.dissipate.beans.AuthTokenVO;
import io.grpc.Context;
import io.grpc.Metadata;

public class AuthenticationConstants {

  public static final Metadata.Key<String> AUTH_HEADER_KEY = Metadata.Key.of("Authentication", Metadata.ASCII_STRING_MARSHALLER);

  public static final Context.Key<AuthTokenVO> CONTEXT_FB_USER_KEY = Context.key("fb_user");

  public static final Context.Key<String> CONTEXT_UID_KEY = Context.key("uid");

  private AuthenticationConstants() {
    // class cannot be instantiated
  }
}
