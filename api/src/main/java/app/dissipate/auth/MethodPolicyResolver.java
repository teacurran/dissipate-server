package app.dissipate.auth;

import app.dissipate.grpc.v1.Common;
import app.dissipate.grpc.v1.MethodPolicy;
import app.dissipate.grpc.v1.Role;
import com.google.protobuf.Descriptors;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.ProtoMethodDescriptorSupplier;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Reads the {@code (dissipate.v1.policy)} option declared on a gRPC method and returns its
 * {@link MethodPolicy}. Results are cached by fully-qualified method name (the policy is static
 * for the life of the process). When a method declares no policy the {@link #DEFAULT_POLICY}
 * (authentication required, no app access) is returned so a missing annotation fails closed.
 */
public final class MethodPolicyResolver {

  /** Fail-closed default for a method with no declared policy: authenticated user, no app access. */
  public static final MethodPolicy DEFAULT_POLICY = MethodPolicy.newBuilder()
      .setAllowUnauthenticated(false)
      .setAllowApp(false)
      .setMinRole(Role.ROLE_USER)
      .build();

  private static final ConcurrentMap<String, MethodPolicy> CACHE = new ConcurrentHashMap<>();

  private MethodPolicyResolver() {
    // utility class
  }

  public static MethodPolicy resolve(MethodDescriptor<?, ?> method) {
    return CACHE.computeIfAbsent(method.getFullMethodName(), name -> extract(method));
  }

  private static MethodPolicy extract(MethodDescriptor<?, ?> method) {
    Object schema = method.getSchemaDescriptor();
    if (schema instanceof ProtoMethodDescriptorSupplier supplier) {
      Descriptors.MethodDescriptor proto = supplier.getMethodDescriptor();
      if (proto.getOptions().hasExtension(Common.policy)) {
        return proto.getOptions().getExtension(Common.policy);
      }
    }
    return DEFAULT_POLICY;
  }
}
