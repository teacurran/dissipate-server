package app.dissipate.api.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Start an OTP sign-up. Provide an email (phone not yet supported). With neither field, a bare
 * anonymous session is created.
 */
@Schema(name = "AuthRegisterRequest", description = "Begin OTP sign-up")
public record AuthRegisterRequest(
  @Schema(description = "Email address to register", example = "user@example.com")
  String email,

  @Schema(description = "Phone number (E.164) — not yet supported", nullable = true)
  String phone
) {
}
