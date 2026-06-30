package app.dissipate.api.grpc;

import app.dissipate.grpc.v1.OAuthService;
import app.dissipate.grpc.v1.TokenRequest;
import app.dissipate.grpc.v1.TokenResponse;
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
public class OAuthServiceImpl implements OAuthService {

  @Inject
  TokenMethod tokenMethod;

  // Token declares MethodPolicy(allow_unauthenticated) — it IS the credential exchange.
  // @WithSession so the app lookup + token persist run in one reactive session.
  @Override
  @WithSession
  public Uni<TokenResponse> token(TokenRequest request) {
    return tokenMethod.token(request);
  }
}
