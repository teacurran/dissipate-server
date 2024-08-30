package app.dissipate.api.grpc;

import app.dissipate.grpc.*;
import app.dissipate.interceptors.GrpcAuthInterceptor;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import io.opentelemetry.instrumentation.annotations.WithSpan;
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
  @WithSpan("DissipateServiceImpl.validateSession")
  public Uni<ValidateSessionResponse> validateSession(ValidateSessionRequest request) {
    return validateSessionMethod.validateSession(request);
  }
}

