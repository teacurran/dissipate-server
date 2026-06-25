package app.dissipate.api.grpc;

import app.dissipate.grpc.v1.GetSessionRequest;
import app.dissipate.grpc.v1.GetSessionResponse;
import app.dissipate.grpc.v1.SessionService;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import app.dissipate.interceptors.GrpcSecurityInterceptor;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

@GrpcService
@RegisterInterceptor(GrpcSecurityInterceptor.class)
@RegisterInterceptor(GrpcLocaleInterceptor.class)
public class SessionServiceImpl implements SessionService {

  @Inject
  GetSessionMethod getSessionMethod;

  @Override
  @RolesAllowed("user")
  public Uni<GetSessionResponse> getSession(GetSessionRequest request) {
    return getSessionMethod.handler(request);
  }
}
