package app.dissipate.api.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Set or change the current account's password.
 *
 * @param currentPassword required only when a password is already set (a change); ignored when
 *                        setting a password for the first time
 * @param newPassword     the new password (min 8 chars)
 */
@Schema(name = "AuthPasswordRequest", description = "Set or change the account password")
public record AuthPasswordRequest(
  @Schema(description = "Current password (required when changing an existing password)", nullable = true)
  String currentPassword,

  @Schema(description = "New password (minimum 8 characters)", required = true)
  @NotBlank @Size(min = 8, max = 256) String newPassword
) {
}
