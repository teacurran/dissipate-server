package app.dissipate.api.rest.auth;

import app.dissipate.api.rest.error.RestApiException;
import app.dissipate.api.rest.error.RestErrorCodes;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * Name-bound to {@link RestAuthenticated}: requires an {@code Authorization: Bearer <session>}
 * header and stashes the token in {@link CurrentSession} for lazy resolution. Replaces the
 * hand-rolled per-endpoint auth block previously in {@code ChatResource}.
 *
 * <p>The DB lookup is intentionally NOT done here — see {@link CurrentSession#require()} for why.
 */
@Provider
@RestAuthenticated
@Priority(Priorities.AUTHENTICATION)
public class RestAuthenticationFilter implements ContainerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  @Inject
  CurrentSession currentSession;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    String authorization = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
    if (authorization == null || authorization.isBlank()) {
      throw new RestApiException(Response.Status.UNAUTHORIZED, RestErrorCodes.AUTH_REQUIRED);
    }

    String token = authorization.startsWith(BEARER_PREFIX)
      ? authorization.substring(BEARER_PREFIX.length()).trim()
      : authorization.trim();

    if (token.isEmpty()) {
      throw new RestApiException(Response.Status.UNAUTHORIZED, RestErrorCodes.AUTH_REQUIRED);
    }

    currentSession.setToken(token);
  }
}
