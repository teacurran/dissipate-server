package app.dissipate.api.grpc;

import app.dissipate.grpc.v1.ChangeIdentityRequest;
import app.dissipate.grpc.v1.ChangeIdentityResponse;
import app.dissipate.grpc.v1.CreateIdentityRequest;
import app.dissipate.grpc.v1.CreateIdentityResponse;
import app.dissipate.grpc.v1.IdentityService;
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
public class IdentityServiceImpl implements IdentityService {

  @Inject
  CreateIdentityMethod createIdentityMethod;

  @Inject
  ChangeIdentityMethod changeIdentityMethod;

  // Both methods declare MethodPolicy(min_role: ROLE_USER); the auth pipeline enforces it.
  // @WithSession so PrincipalResolver.authorize() and the entity work share one reactive session.
  @Override
  @WithSession
  public Uni<CreateIdentityResponse> createIdentity(CreateIdentityRequest request) {
    return createIdentityMethod.create(request);
  }

  @Override
  @WithSession
  public Uni<ChangeIdentityResponse> changeIdentity(ChangeIdentityRequest request) {
    return changeIdentityMethod.change(request);
  }
}
