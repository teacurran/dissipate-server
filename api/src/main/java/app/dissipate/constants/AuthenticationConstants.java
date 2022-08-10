package app.dissipate.constants;

import com.google.firebase.auth.FirebaseToken;
import io.grpc.Context;
import io.grpc.Metadata;

public class AuthenticationConstants {

    public static final Metadata.Key<String> META_DATA_KEY = Metadata.Key.of("Authentication", Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<FirebaseToken> CONTEXT_FB_USER_KEY = Context.key("fb_user");
}