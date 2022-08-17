package app.dissipate.constants;

import io.grpc.Context;
import io.grpc.Metadata;

public class AuthenticationConstants {

    public static final Metadata.Key<String> AUTH_HEADER_KEY = Metadata.Key.of("Authentication", Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<String> CONTEXT_FB_USER_KEY = Context.key("fb_user");
}