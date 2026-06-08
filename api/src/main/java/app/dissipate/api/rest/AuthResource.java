package app.dissipate.api.rest;

import app.dissipate.api.rest.auth.CurrentSession;
import app.dissipate.api.rest.auth.RestAuthenticated;
import app.dissipate.api.rest.dto.ApiErrorResponse;
import app.dissipate.api.rest.dto.AuthLoginRequest;
import app.dissipate.api.rest.dto.AuthLoginResponse;
import app.dissipate.api.rest.dto.AuthPasswordRequest;
import app.dissipate.api.rest.dto.AuthRegisterRequest;
import app.dissipate.api.rest.dto.AuthRegisterResponse;
import app.dissipate.api.rest.dto.AuthSessionResponse;
import app.dissipate.api.rest.dto.AuthVerifyRequest;
import app.dissipate.api.rest.dto.AuthVerifyResponse;
import app.dissipate.services.auth.AuthService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * Public auth/registration endpoints (OTP sign-up). Authenticated auth endpoints (logout, session,
 * password, session management) land in later slices. Each method runs in its own transaction so
 * the audit row commits atomically with the action; error outcomes are returned as {@code Response}
 * values (not thrown) so audit rows and the OTP attempt counter are not rolled back.
 */
@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

  @Inject
  AuthService authService;

  @Inject
  CurrentSession currentSession;

  @Inject
  RoutingContext routingContext;

  @POST
  @Path("/register")
  @WithTransaction
  @APIResponse(responseCode = "200", description = "Sign-up started",
    content = @Content(schema = @Schema(implementation = AuthRegisterResponse.class)))
  @APIResponse(responseCode = "409", description = "Email already registered",
    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
  public Uni<Response> register(AuthRegisterRequest body) {
    return authService.register(
      body == null ? null : body.email(),
      body == null ? null : body.phone(),
      clientIp(),
      userAgent());
  }

  @POST
  @Path("/verify")
  @WithTransaction
  @APIResponse(responseCode = "200", description = "OTP verified; returns Bearer token",
    content = @Content(schema = @Schema(implementation = AuthVerifyResponse.class)))
  @APIResponse(responseCode = "400", description = "OTP invalid, expired, or exhausted",
    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
  public Uni<Response> verify(@Valid AuthVerifyRequest body) {
    return authService.verify(body.sid(), body.otp(), clientIp(), userAgent());
  }

  @POST
  @Path("/login")
  @WithTransaction
  @APIResponse(responseCode = "200", description = "Login succeeded; returns Bearer token",
    content = @Content(schema = @Schema(implementation = AuthLoginResponse.class)))
  @APIResponse(responseCode = "401", description = "Invalid email or password",
    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
  @APIResponse(responseCode = "429", description = "Account temporarily locked",
    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
  public Uni<Response> login(@Valid AuthLoginRequest body) {
    return authService.login(body.email(), body.password(), clientIp(), userAgent());
  }

  @POST
  @Path("/password")
  @RestAuthenticated
  @WithTransaction
  @APIResponse(responseCode = "204", description = "Password set or changed")
  @APIResponse(responseCode = "403", description = "Current password incorrect",
    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
  public Uni<Response> setPassword(@Valid AuthPasswordRequest body) {
    return currentSession.require().onItem().transformToUni(session ->
      authService.setPassword(session.account, body.currentPassword(), body.newPassword(),
        clientIp(), userAgent()));
  }

  @POST
  @Path("/logout")
  @RestAuthenticated
  @WithTransaction
  @APIResponse(responseCode = "204", description = "Session ended")
  public Uni<Response> logout() {
    return currentSession.require().onItem().transformToUni(session ->
      authService.logout(session, clientIp(), userAgent()));
  }

  @GET
  @Path("/session")
  @RestAuthenticated
  @WithSession
  @APIResponse(responseCode = "200", description = "Current session snapshot",
    content = @Content(schema = @Schema(implementation = AuthSessionResponse.class)))
  public Uni<AuthSessionResponse> session() {
    return currentSession.require().onItem().transform(session -> new AuthSessionResponse(
      session.id.toString(),
      session.account != null ? session.account.status.name() : null,
      session.identity != null ? session.identity.id : null,
      session.loggedIn));
  }

  private String clientIp() {
    if (routingContext.request() != null && routingContext.request().remoteAddress() != null) {
      return routingContext.request().remoteAddress().hostAddress();
    }
    return null;
  }

  private String userAgent() {
    return routingContext.request() != null
      ? routingContext.request().getHeader(HttpHeaders.USER_AGENT)
      : null;
  }
}
