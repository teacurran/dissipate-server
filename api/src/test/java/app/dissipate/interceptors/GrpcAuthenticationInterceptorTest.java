package app.dissipate.interceptors;

import app.dissipate.auth.PrincipalResolver;
import app.dissipate.constants.AuthenticationConstants;
import app.dissipate.grpc.v1.AccountServiceGrpc;
import app.dissipate.grpc.v1.GetSessionRequest;
import app.dissipate.grpc.v1.GetSessionResponse;
import app.dissipate.grpc.v1.RegisterRequest;
import app.dissipate.grpc.v1.RegisterResponse;
import app.dissipate.grpc.v1.SessionServiceGrpc;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.Status;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Unit coverage for the synchronous half of the auth pipeline: bearer-token extraction (with/without
 * the scheme prefix), the fail-closed rejection of a protected method with no token, and binding the
 * resolved policy + token into the request-scoped resolver.
 */
class GrpcAuthenticationInterceptorTest {

  private static final Metadata.Key<String> AUTH = AuthenticationConstants.AUTHORIZATION_HEADER_KEY;

  private static final class FakeServerCall<Q, R> extends ServerCall<Q, R> {
    private final MethodDescriptor<Q, R> descriptor;
    private Status closeStatus;

    FakeServerCall(MethodDescriptor<Q, R> descriptor) {
      this.descriptor = descriptor;
    }

    @Override public void request(int numMessages) { }
    @Override public void sendHeaders(Metadata headers) { }
    @Override public void sendMessage(R message) { }
    @Override public void close(Status status, Metadata trailers) { this.closeStatus = status; }
    @Override public boolean isCancelled() { return false; }
    @Override public MethodDescriptor<Q, R> getMethodDescriptor() { return descriptor; }
  }

  private static final class FakeHandler<Q, R> implements ServerCallHandler<Q, R> {
    private boolean started;

    @Override
    public ServerCall.Listener<Q> startCall(ServerCall<Q, R> call, Metadata headers) {
      started = true;
      return new ServerCall.Listener<>() { };
    }
  }

  private static GrpcAuthenticationInterceptor interceptor(PrincipalResolver resolver) {
    GrpcAuthenticationInterceptor interceptor = new GrpcAuthenticationInterceptor();
    interceptor.principalResolver = resolver;
    return interceptor;
  }

  @Test
  void protectedMethodWithoutTokenIsClosedUnauthenticated() {
    PrincipalResolver resolver = Mockito.mock(PrincipalResolver.class);
    FakeServerCall<GetSessionRequest, GetSessionResponse> call =
        new FakeServerCall<>(SessionServiceGrpc.getGetSessionMethod());
    FakeHandler<GetSessionRequest, GetSessionResponse> handler = new FakeHandler<>();

    interceptor(resolver).interceptCall(call, new Metadata(), handler);

    assertEquals(Status.Code.UNAUTHENTICATED, call.closeStatus.getCode());
    assertFalse(handler.started, "handler must not run for a rejected call");
    Mockito.verify(resolver, Mockito.never()).bind(any(), any());
  }

  @Test
  void bearerTokenIsExtractedAndBound() {
    PrincipalResolver resolver = Mockito.mock(PrincipalResolver.class);
    FakeServerCall<GetSessionRequest, GetSessionResponse> call =
        new FakeServerCall<>(SessionServiceGrpc.getGetSessionMethod());
    FakeHandler<GetSessionRequest, GetSessionResponse> handler = new FakeHandler<>();
    Metadata md = new Metadata();
    md.put(AUTH, "Bearer abc123");

    interceptor(resolver).interceptCall(call, md, handler);

    assertNull(call.closeStatus);
    assertTrue(handler.started);
    Mockito.verify(resolver).bind(any(), eq("abc123"));
  }

  @Test
  void rawTokenWithoutSchemeIsTolerated() {
    PrincipalResolver resolver = Mockito.mock(PrincipalResolver.class);
    FakeServerCall<GetSessionRequest, GetSessionResponse> call =
        new FakeServerCall<>(SessionServiceGrpc.getGetSessionMethod());
    Metadata md = new Metadata();
    md.put(AUTH, "rawtoken");

    interceptor(resolver).interceptCall(call, md, new FakeHandler<>());

    Mockito.verify(resolver).bind(any(), eq("rawtoken"));
  }

  @Test
  void unauthenticatedMethodProceedsWithEmptyToken() {
    PrincipalResolver resolver = Mockito.mock(PrincipalResolver.class);
    FakeServerCall<RegisterRequest, RegisterResponse> call =
        new FakeServerCall<>(AccountServiceGrpc.getRegisterMethod());
    FakeHandler<RegisterRequest, RegisterResponse> handler = new FakeHandler<>();

    interceptor(resolver).interceptCall(call, new Metadata(), handler);

    assertNull(call.closeStatus);
    assertTrue(handler.started);
    Mockito.verify(resolver).bind(any(), eq(""));
  }
}
