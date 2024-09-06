package app.dissipate.api.grpc;

import app.dissipate.exceptions.ApiException;
import app.dissipate.grpc.CreateHandleRequest;
import app.dissipate.grpc.CreateHandleResponse;
import app.dissipate.grpc.DissipateService;
import app.dissipate.grpc.GetSessionRequest;
import app.dissipate.grpc.GetSessionResponse;
import app.dissipate.grpc.RegisterRequest;
import app.dissipate.grpc.RegisterResponse;
import app.dissipate.grpc.ValidateSessionRequest;
import app.dissipate.grpc.ValidateSessionResponse;
import app.dissipate.interceptors.GrpcAuthInterceptor;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

@GrpcService
@RegisterInterceptor(GrpcAuthInterceptor.class)
@RegisterInterceptor(GrpcLocaleInterceptor.class)
public class DissipateServiceImpl implements DissipateService {

  @Inject
  RegisterMethod registerMethod;

  @Inject
  ValidateSessionMethod validateSessionMethod;

  @Inject
  GetSessionMethod getSessionMethod;

  @Override
  @WithSession
  public Uni<GetSessionResponse> getSession(GetSessionRequest request) {
    return getSessionMethod.handler(request);
  }


  @Override
  @WithSession
  @WithTransaction
  public Uni<RegisterResponse> register(RegisterRequest request) {
    return registerMethod.register(request);
  }

  @Override
  @WithSession
  public Uni<CreateHandleResponse> createHandle(CreateHandleRequest request) {
    return Uni.createFrom().item(CreateHandleResponse.newBuilder().setHandle(request.getHandle()).build());
  }

  @Override
  @WithSession
  @WithTransaction
  public Uni<ValidateSessionResponse> validateSession(ValidateSessionRequest request) throws ApiException {
    return validateSessionMethod.validateSession(request);
  }
}

