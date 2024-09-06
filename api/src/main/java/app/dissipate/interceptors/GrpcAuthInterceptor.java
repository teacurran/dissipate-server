package app.dissipate.interceptors;

import app.dissipate.constants.AuthenticationConstants;
import io.grpc.*;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GrpcAuthInterceptor implements ServerInterceptor {

    @Override
    @WithSpan("grpc-auth-interceptor")
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String token = metadata.get(AuthenticationConstants.AUTH_HEADER_KEY);



        final Context context = Context.current();

        //serverCall.
        //identityAssociation.getDeferredIdentity().await().indefinitely();

        return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
    }
}
