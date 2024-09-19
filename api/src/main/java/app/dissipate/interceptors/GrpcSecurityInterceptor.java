package app.dissipate.interceptors;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.SocketAddress;

@ApplicationScoped
public class GrpcSecurityInterceptor implements ServerInterceptor {

  public static final String CLIENT_IP_KEY_NAME = "client-ip";

  public static final Context.Key<String> CLIENT_IP_KEY = Context.key(CLIENT_IP_KEY_NAME);

  @SuppressWarnings("java:S119")
  @Override
  @WithSpan("grpc-auth-interceptor")
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
    Context context = Context.current();

    // Extract client IP address
    SocketAddress clientIp = serverCall.getAttributes()
      .get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
    if (clientIp != null) {
      context = context.withValue(CLIENT_IP_KEY, clientIp.toString());
    }

    return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
  }
}
