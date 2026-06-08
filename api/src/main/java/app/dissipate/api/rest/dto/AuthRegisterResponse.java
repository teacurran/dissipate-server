package app.dissipate.api.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Result of starting sign-up.
 *
 * @param sid    the session id (UUID) — becomes the Bearer token once verified
 * @param result {@code SESSION_CREATED} (bare anonymous session) or {@code EMAIL_SENT} (OTP issued)
 */
@Schema(name = "AuthRegisterResponse", description = "Result of starting OTP sign-up")
public record AuthRegisterResponse(
  @Schema(description = "Session id (UUID); becomes the Bearer token after verification")
  String sid,

  @Schema(description = "Outcome", enumeration = {"SESSION_CREATED", "EMAIL_SENT"}, example = "EMAIL_SENT")
  String result
) {
}
