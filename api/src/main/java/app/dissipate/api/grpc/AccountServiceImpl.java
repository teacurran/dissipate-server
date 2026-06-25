package app.dissipate.api.grpc;

import app.dissipate.exceptions.ApiException;
import app.dissipate.grpc.v1.AccountService;
import app.dissipate.grpc.v1.RegisterRequest;
import app.dissipate.grpc.v1.RegisterResponse;
import app.dissipate.grpc.v1.ValidateSessionRequest;
import app.dissipate.grpc.v1.ValidateSessionResponse;
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
public class AccountServiceImpl implements AccountService {

  @Inject
  RegisterMethod registerMethod;

  @Inject
  ValidateSessionMethod validateSessionMethod;

  @Override
  @WithSession
  public Uni<RegisterResponse> register(RegisterRequest request) {
    return registerMethod.register(request);
  }

  @Override
  @WithSession
  public Uni<ValidateSessionResponse> validateSession(ValidateSessionRequest request) throws ApiException {
    return validateSessionMethod.validateSession(request);
  }
}
