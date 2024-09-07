package app.dissipate.interceptors;

import app.dissipate.data.models.Session;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DissipateIdentityProvider implements IdentityProvider<TokenAuthenticationRequest> {

  private static final org.jboss.logging.Logger LOGGER = org.jboss.logging.Logger.getLogger(DissipateIdentityProvider.class);

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
    return Session.findBySidValidated(token)
      .onFailure().invoke(t -> LOGGER.error("session not found", t))
      .onItem().transform(session -> {
        if (session != null) {
          return QuarkusSecurityIdentity.builder()
            .setPrincipal(new QuarkusPrincipal(token))
            .addRole("user").build();
        } else {
          otel.addEvent("session not found");
          return null;
        }
      });
  }
}
