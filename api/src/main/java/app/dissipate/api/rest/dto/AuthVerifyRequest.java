package app.dissipate.api.rest.dto;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Complete sign-up by submitting the OTP for a session. */
@Schema(name = "AuthVerifyRequest", description = "Verify an OTP")
public record AuthVerifyRequest(
  @Schema(description = "Session id returned by /register", required = true)
  @NotBlank String sid,

  @Schema(description = "One-time password from the email", required = true, example = "K7X9QP")
  @NotBlank String otp
) {
}
