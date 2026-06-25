package app.dissipate.api.grpc;

import app.dissipate.grpc.v1.GetSessionRequest;
import app.dissipate.grpc.v1.GetSessionResponse;
import app.dissipate.grpc.v1.SessionService;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import app.dissipate.interceptors.GrpcSecurityInterceptor;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

@GrpcService
@RegisterInterceptor(GrpcSecurityInterceptor.class)
@RegisterInterceptor(GrpcLocaleInterceptor.class)
public class SessionServiceImpl implements SessionService {

  @Inject
  GetSessionMethod getSessionMethod;

  // Authorization is enforced by the auth pipeline (GrpcAuthenticationInterceptor +
  // PrincipalResolver.authorize()) against this method's declared MethodPolicy(min_role: ROLE_USER).
  // @WithSession so the resolver's session lookup runs in the handler's reactive session.
  @Override
  @WithSession
  public Uni<GetSessionResponse> getSession(GetSessionRequest request) {
    return getSessionMethod.handler(request);
  }
}
