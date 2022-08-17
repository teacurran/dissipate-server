package app.dissipate.interceptors;

import app.dissipate.constants.AuthenticationConstants;
import app.dissipate.services.AuthenticationService;
import com.google.common.base.Strings;
import io.grpc.*;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class GrpcAuthInterceptor implements ServerInterceptor {

    private static final Logger LOG = Logger.getLogger(GrpcAuthInterceptor.class);

    @Inject
    AuthenticationService authenticationService;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String token = metadata.get(AuthenticationConstants.AUTH_HEADER_KEY);

        Context context = Context.current();

        if (!Strings.isNullOrEmpty(token)) {
            String fbToken = authenticationService.verifyIdToken(token);
            context = context.withValue(AuthenticationConstants.CONTEXT_FB_USER_KEY, fbToken);
        } else {
            serverCall.close(Status.UNAUTHENTICATED.withDescription("Auth Token Required"), metadata);
        }

        return Contexts.interceptCall(
                context,
                serverCall,
                metadata,
                serverCallHandler);
    }
}
