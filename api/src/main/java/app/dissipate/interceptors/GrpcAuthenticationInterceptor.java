package app.dissipate.interceptors;

import app.dissipate.auth.MethodPolicyResolver;
import app.dissipate.auth.PrincipalResolver;
import app.dissipate.constants.AuthenticationConstants;
import app.dissipate.grpc.v1.MethodPolicy;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.grpc.GlobalInterceptor;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Stage 1 of the auth pipeline (synchronous, global). For every gRPC call it:
 * <ol>
 *   <li>resolves the method's {@link MethodPolicy} from the proto descriptor,</li>
 *   <li>extracts the Bearer token from the {@code authorization} metadata header,</li>
 *   <li>stashes both in the gRPC {@link Context} for the reactive
 *       {@link app.dissipate.auth.PrincipalResolver} to enforce inside the call's Hibernate
 *       session (the DB lookup cannot run here — there is no active reactive session yet,
 *       mirroring the REST layer's lazy {@code CurrentSession} resolution), and</li>
 *   <li>performs the one check needing no database access: a protected method invoked with no
 *       token is rejected with {@code UNAUTHENTICATED} before the handler runs.</li>
 * </ol>
 */
@GlobalInterceptor
@ApplicationScoped
@Priority(100)
public class GrpcAuthenticationInterceptor implements ServerInterceptor {

  @Inject
  PrincipalResolver principalResolver;

  @SuppressWarnings("java:S119")
  @Override
  @WithSpan("grpc-authn")
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {

    MethodPolicy policy = MethodPolicyResolver.resolve(call.getMethodDescriptor());
    String token = extractBearer(metadata);

    if (!policy.getAllowUnauthenticated() && (token == null || token.isBlank())) {
      call.close(Status.UNAUTHENTICATED.withDescription("missing bearer token"), new Metadata());
      return new ServerCall.Listener<>() {
        // no-op: the call has already been closed
      };
    }

    // Bind into the request-scoped resolver synchronously: the gRPC context below is not current
    // once the handler runs inside its @WithSession reactive continuation, but this bean is.
    principalResolver.bind(policy, token == null ? "" : token);

    Context context = Context.current()
        .withValue(AuthenticationConstants.POLICY_KEY, policy)
        .withValue(AuthenticationConstants.BEARER_TOKEN_KEY, token == null ? "" : token);

    return Contexts.interceptCall(context, call, metadata, next);
  }

  /** Pull the token out of {@code Authorization: Bearer <sid>}, tolerating a bare token. */
  private static String extractBearer(Metadata metadata) {
    String header = metadata.get(AuthenticationConstants.AUTHORIZATION_HEADER_KEY);
    if (header == null) {
      return null;
    }
    String trimmed = header.trim();
    String prefix = AuthenticationConstants.BEARER_PREFIX;
    if (trimmed.regionMatches(true, 0, prefix, 0, prefix.length())) {
      return trimmed.substring(prefix.length()).trim();
    }
    return trimmed;
  }
}
