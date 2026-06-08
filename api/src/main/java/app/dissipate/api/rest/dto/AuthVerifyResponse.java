package app.dissipate.api.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Result of a successful verification.
 *
 * @param token the usable Bearer token (the session id) for subsequent authenticated calls
 */
@Schema(name = "AuthVerifyResponse", description = "Result of OTP verification")
public record AuthVerifyResponse(
  @Schema(description = "Bearer token (session id) for authenticated requests")
  String token
) {
}
