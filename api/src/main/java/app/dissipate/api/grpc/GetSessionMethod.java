package app.dissipate.api.grpc;

import app.dissipate.data.models.Session;
import app.dissipate.grpc.GetSessionRequest;
import app.dissipate.grpc.GetSessionResponse;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GetSessionMethod {

  @Inject
  CurrentIdentityAssociation identity;

  @WithSpan("GetSessionMethod.handler")
  @WithSession
  public Uni<GetSessionResponse> handler(GetSessionRequest request) {
    return identity.getDeferredIdentity().onItem().transformToUni(si -> {
      Session session = si.getAttribute("session");

      GetSessionResponse.Builder rb = GetSessionResponse.newBuilder().setSid(si.getPrincipal().getName());
      if (session != null && session.identity != null) {
        rb.setIid(session.identity.id);
      }
      return Uni.createFrom().item(rb.build());
    });
  }
}
