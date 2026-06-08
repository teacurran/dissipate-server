package app.dissipate.api.rest.dto;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Password login. */
@Schema(name = "AuthLoginRequest", description = "Email + password login")
public record AuthLoginRequest(
  @Schema(description = "Account email", required = true, example = "user@example.com")
  @NotBlank String email,

  @Schema(description = "Account password", required = true)
  @NotBlank String password
) {
}
