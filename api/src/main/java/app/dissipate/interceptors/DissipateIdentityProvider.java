package app.dissipate.interceptors;

import app.dissipate.data.models.Session;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DissipateIdentityProvider implements IdentityProvider<TokenAuthenticationRequest> {

  @Override
  public Class getRequestType() {
    return TokenAuthenticationRequest.class;
  }

  @Override
  @WithSession
  @WithSpan("DissipateIdentityProvider.authenticate")
  public Uni<SecurityIdentity> authenticate(TokenAuthenticationRequest request, AuthenticationRequestContext context) {
    Span otel = Span.current();
    String token = request.getToken().getToken();
    otel.setAttribute("token", token);
    return Session.findBySidValidated(token).onItem().transform(session -> {
      if (session != null) {
        QuarkusSecurityIdentity identity = QuarkusSecurityIdentity.builder().addRole("user").build();

        return identity;
      } else {
        return null;
      }
    });
  }
}
