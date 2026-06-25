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

  @Override
  public Uni<CreateIdentityResponse> createIdentity(CreateIdentityRequest request) {
    return createIdentityMethod.create(request);
  }

  @Override
  public Uni<ChangeIdentityResponse> changeIdentity(ChangeIdentityRequest request) {
    return changeIdentityMethod.change(request);
  }
}
