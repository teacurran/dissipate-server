package app.dissipate.api.rest.auth;

import app.dissipate.api.rest.error.RestApiException;
import app.dissipate.api.rest.error.RestErrorCodes;
import app.dissipate.data.models.Identity;
import app.dissipate.data.models.Session;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Response;

/**
 * Request-scoped holder for the authenticated session. {@link RestAuthenticationFilter} stashes the
 * raw Bearer token; resources call {@link #require()} to resolve and cache the {@link Session}.
 *
 * <p>Resolution is deliberately lazy rather than done in the filter: {@code require()} runs inside
 * the resource's own reactive/Hibernate session (its {@code @WithTransaction}/{@code @WithSession}),
 * so the returned {@link Session} and its fetched {@code identity} are managed by the same session
 * the resource persists in — avoiding cross-session entity errors.
 */
@RequestScoped
public class CurrentSession {

  private String token;
  private Session session;

  void setToken(String token) {
    this.token = token;
  }

  public String getToken() {
    return token;
  }

  /**
   * Resolve (and cache) the validated session for the request's Bearer token. Fails with a 401
   * {@link RestApiException} if the token is missing, malformed, or does not map to a live,
   * validated session.
   */
  public Uni<Session> require() {
    if (session != null) {
      return Uni.createFrom().item(session);
    }
    if (token == null || token.isBlank()) {
      return Uni.createFrom().failure(
        new RestApiException(Response.Status.UNAUTHORIZED, RestErrorCodes.AUTH_REQUIRED));
    }
    // findBySidValidated parses the token as a UUID and can throw synchronously on a malformed
    // value; defer so that surfaces as a Uni failure we can map to a clean 401.
    return Uni.createFrom().deferred(() -> Session.findBySidValidated(token))
      .onFailure(IllegalArgumentException.class).transform(e ->
        new RestApiException(Response.Status.UNAUTHORIZED, RestErrorCodes.AUTH_SESSION_INVALID))
      .onItem().ifNull().failWith(() ->
        new RestApiException(Response.Status.UNAUTHORIZED, RestErrorCodes.AUTH_SESSION_INVALID))
      .onItem().invoke(resolved -> this.session = resolved);
  }

  /**
   * Resolve the session and assert an identity is selected, returning it. Fails with 403 if the
   * session has no active identity.
   */
  public Uni<Identity> requireIdentity() {
    return require().onItem().transform(resolved -> {
      if (resolved.identity == null) {
        throw new RestApiException(Response.Status.FORBIDDEN, RestErrorCodes.AUTH_IDENTITY_REQUIRED);
      }
      return resolved.identity;
    });
  }
}
