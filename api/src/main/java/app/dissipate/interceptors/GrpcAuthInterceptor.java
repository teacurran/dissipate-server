package app.dissipate.interceptors;

import app.dissipate.Main;
import app.dissipate.constants.AuthenticationConstants;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.base.Strings;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import io.grpc.*;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GrpcAuthInterceptor implements ServerInterceptor {

    private static final Logger LOG = Logger.getLogger(GrpcAuthInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String token = metadata.get(AuthenticationConstants.META_DATA_KEY);

        Context context = Context.current();

        if (!Strings.isNullOrEmpty(token)) {
            try {
                FirebaseToken fbToken = FirebaseAuth.getInstance().verifyIdToken(token);

                context = context.withValue(AuthenticationConstants.CONTEXT_FB_USER_KEY, fbToken);
            } catch (FirebaseAuthException e) {
                LOG.errorv(e,"Failed to verify token: '{0}'", token);
                serverCall.close(Status.UNAUTHENTICATED.withDescription("Auth Token Invalid"), metadata);
            }
        }

        return Contexts.interceptCall(
                context,
                serverCall,
                metadata,
                serverCallHandler);
    }
}
