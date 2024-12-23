package app.dissipate.api.grpc;

import app.dissipate.exceptions.ApiException;
import app.dissipate.grpc.*;
import app.dissipate.interceptors.GrpcSecurityInterceptor;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

@GrpcService
@RegisterInterceptor(GrpcSecurityInterceptor.class)
@RegisterInterceptor(GrpcLocaleInterceptor.class)
public class DissipateServiceImpl implements DissipateService {

  @Inject
  RegisterMethod registerMethod;

  @Inject
  ValidateSessionMethod validateSessionMethod;

  @Inject
  GetSessionMethod getSessionMethod;

  @Inject
  RunEtlLocationMethod runEtlLocationMethod;

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

  @Override
  @WithSession
  @RolesAllowed("user")
  public Uni<GetSessionResponse> getSession(GetSessionRequest request) {
    return getSessionMethod.handler(request);
  }

  @Override
  @WithSession
  public Uni<RegisterResponse> register(RegisterRequest request) {
    return registerMethod.register(request);
  }

  @Override
  @WithSession
  public Uni<RunEtlLocationResponse> runEtlLocation(RunEtlLocationRequest request) {
    return runEtlLocationMethod.run(request);
  }

  @Override
  @WithSession
  public Uni<ValidateSessionResponse> validateSession(ValidateSessionRequest request) throws ApiException {
    return validateSessionMethod.validateSession(request);
  }

  @Override
  public Multi<GetChatsResponse> getChats(GetChatsRequest request) {
    return null;
  }
}

