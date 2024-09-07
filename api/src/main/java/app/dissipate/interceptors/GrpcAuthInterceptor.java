package app.dissipate.interceptors;

import io.grpc.*;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GrpcAuthInterceptor implements ServerInterceptor {

    @Override
    @WithSpan("grpc-auth-interceptor")
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        final Context context = Context.current();

        return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
    }
}
