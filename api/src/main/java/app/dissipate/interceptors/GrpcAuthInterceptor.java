package app.dissipate.interceptors;

import app.dissipate.beans.FirebaseTokenVO;
import app.dissipate.constants.AuthenticationConstants;
import app.dissipate.services.AuthenticationService;
import com.google.common.base.Strings;
import io.grpc.*;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GrpcAuthInterceptor implements ServerInterceptor {

    @Inject
    AuthenticationService authenticationService;

    @Override
    @WithSpan("grpc-auth-interceptor")
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String token = metadata.get(AuthenticationConstants.AUTH_HEADER_KEY);
        Context context = Context.current();
//        boolean mustClose = false;
        try {
            if (!Strings.isNullOrEmpty(token)) {
                FirebaseTokenVO fbToken = authenticationService.verifyIdToken(token);
                context = context.withValue(AuthenticationConstants.CONTEXT_FB_USER_KEY, fbToken);
                context = context.withValue(AuthenticationConstants.CONTEXT_UID_KEY, fbToken.getUid());

                serverCall.close(Status.UNAUTHENTICATED.withDescription("Auth Token Required"), metadata);
                return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
            } else {
//                mustClose = true;
                return new ServerCall.Listener<>() {
                };
            }
        } finally {
//            if (mustClose) {
//                //serverCall.close(Status.UNAUTHENTICATED.withDescription("Auth Token Required"), metadata);
//            }
        }
    }
}
