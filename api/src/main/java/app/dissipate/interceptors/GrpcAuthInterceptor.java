package app.dissipate.interceptors;

import io.grpc.*;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import io.quarkus.security.identity.CurrentIdentityAssociation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GrpcAuthInterceptor implements ServerInterceptor {

    @Inject
    CurrentIdentityAssociation identityAssociation;

    @Override
    @WithSpan("grpc-auth-interceptor")
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        //String token = metadata.get(AuthenticationConstants.AUTH_HEADER_KEY);
        final Context context = Context.current();

        //serverCall.
        //identityAssociation.getDeferredIdentity().await().indefinitely();

        return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
    }
}
