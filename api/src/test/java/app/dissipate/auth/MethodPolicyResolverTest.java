package app.dissipate.auth;

import app.dissipate.grpc.v1.AccountServiceGrpc;
import app.dissipate.grpc.v1.ChatServiceGrpc;
import app.dissipate.grpc.v1.IdentityServiceGrpc;
import app.dissipate.grpc.v1.MethodPolicy;
import app.dissipate.grpc.v1.Role;
import app.dissipate.grpc.v1.GetSessionRequest;
import app.dissipate.grpc.v1.GetSessionResponse;
import app.dissipate.grpc.v1.SessionServiceGrpc;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.ProtoUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the per-method MethodPolicy options declared in the protos are read back correctly
 * off the generated gRPC method descriptors. Resolution is what the auth pipeline enforces, so a
 * regression here would silently weaken authorization.
 */
class MethodPolicyResolverTest {

  @Test
  void registerIsUnauthenticated() {
    MethodPolicy policy = MethodPolicyResolver.resolve(AccountServiceGrpc.getRegisterMethod());
    assertTrue(policy.getAllowUnauthenticated());
    assertEquals(5, policy.getCost());
  }

  @Test
  void validateSessionIsUnauthenticated() {
    MethodPolicy policy = MethodPolicyResolver.resolve(AccountServiceGrpc.getValidateSessionMethod());
    assertTrue(policy.getAllowUnauthenticated());
  }

  @Test
  void getSessionRequiresUserRole() {
    MethodPolicy policy = MethodPolicyResolver.resolve(SessionServiceGrpc.getGetSessionMethod());
    assertFalse(policy.getAllowUnauthenticated());
    assertEquals(Role.ROLE_USER, policy.getMinRole());
    assertFalse(policy.getAllowApp());
  }

  @Test
  void createIdentityRequiresUserRoleWithCost() {
    MethodPolicy policy = MethodPolicyResolver.resolve(IdentityServiceGrpc.getCreateIdentityMethod());
    assertFalse(policy.getAllowUnauthenticated());
    assertEquals(Role.ROLE_USER, policy.getMinRole());
    assertEquals(3, policy.getCost());
  }

  @Test
  void getChatsRequiresUserRole() {
    MethodPolicy policy = MethodPolicyResolver.resolve(ChatServiceGrpc.getGetChatsMethod());
    assertFalse(policy.getAllowUnauthenticated());
    assertEquals(Role.ROLE_USER, policy.getMinRole());
  }

  @Test
  void resolutionIsCachedPerMethod() {
    MethodPolicy first = MethodPolicyResolver.resolve(SessionServiceGrpc.getGetSessionMethod());
    MethodPolicy second = MethodPolicyResolver.resolve(SessionServiceGrpc.getGetSessionMethod());
    assertEquals(first, second);
  }

  @Test
  void methodWithoutProtoSchemaFailsClosedToDefault() {
    // A descriptor with no proto schema descriptor (so no policy option) must fall back to the
    // fail-closed default rather than allow the call.
    MethodDescriptor<GetSessionRequest, GetSessionResponse> noSchema =
        MethodDescriptor.<GetSessionRequest, GetSessionResponse>newBuilder()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName("dissipate.v1.Test/NoSchema")
            .setRequestMarshaller(ProtoUtils.marshaller(GetSessionRequest.getDefaultInstance()))
            .setResponseMarshaller(ProtoUtils.marshaller(GetSessionResponse.getDefaultInstance()))
            .build();

    assertSame(MethodPolicyResolver.DEFAULT_POLICY, MethodPolicyResolver.resolve(noSchema));
  }
}
