package app.dissipate.api.rest;

import app.dissipate.api.rest.dto.ApiErrorResponse;
import app.dissipate.api.rest.dto.AuthRegisterRequest;
import app.dissipate.api.rest.dto.AuthRegisterResponse;
import app.dissipate.api.rest.dto.AuthVerifyRequest;
import app.dissipate.api.rest.dto.AuthVerifyResponse;
import app.dissipate.services.auth.AuthService;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
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
