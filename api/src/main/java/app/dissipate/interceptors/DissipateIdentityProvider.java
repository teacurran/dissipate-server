package app.dissipate.interceptors;

import app.dissipate.data.models.AccountStatus;
import app.dissipate.data.models.Session;
import app.dissipate.services.LocalizationService;
import io.grpc.Status;
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
import jakarta.inject.Inject;

import java.util.Locale;
import java.util.UUID;

import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_TOKEN_INVALID;

@ApplicationScoped
public class DissipateIdentityProvider implements IdentityProvider<TokenAuthenticationRequest> {

  private static final org.jboss.logging.Logger LOGGER = org.jboss.logging.Logger.getLogger(DissipateIdentityProvider.class);

  @Inject
  LocalizationService localizationService;

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
    Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();

    UUID sid;
    try {
      sid = UUID.fromString(token);
    } catch (IllegalArgumentException e) {
      otel.addEvent("invalid token");
      return Uni.createFrom().failure(localizationService.getApiException(locale, Status.NOT_FOUND, AUTH_TOKEN_INVALID));
    }

    return Session.findBySid(sid)
      .onFailure().invoke(t -> LOGGER.error("session not found", t))
      .onItem().transform(session -> {
        if (session != null) {
           QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder()
            .setPrincipal(new QuarkusPrincipal(token));

           if (session.account != null && AccountStatus.ACTIVE.equals(session.account.status)) {
             builder.addRole("user");
           }

           builder.addAttribute("session", session);

           return builder.build();
        } else {
          otel.addEvent("session not found");
          return null;
        }
      });
  }
}
