package app.dissipate.api.grpc;

import app.dissipate.grpc.GetSessionRequest;
import app.dissipate.grpc.GetSessionResponse;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GetSessionMethod {

  @Inject
  CurrentIdentityAssociation identity;

  @WithSpan("GetSessionMethod.handler")
  public Uni<GetSessionResponse> handler(GetSessionRequest request) {
    return identity.getDeferredIdentity().onItem().transformToUni(si -> {
      GetSessionResponse.Builder rb = GetSessionResponse.newBuilder()
        .setSid(si.getPrincipal().getName());
      return Uni.createFrom().item(rb.build());
    });
  }
}
