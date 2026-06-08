package app.dissipate.api.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Result of a successful login.
 *
 * @param token the Bearer token (new session id) for authenticated requests
 */
@Schema(name = "AuthLoginResponse", description = "Result of a successful login")
public record AuthLoginResponse(
  @Schema(description = "Bearer token (session id) for authenticated requests")
  String token
) {
}
