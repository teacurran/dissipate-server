package app.dissipate.api.rest.dto;

import app.dissipate.utils.SnowflakeBase36Serializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Current session snapshot.
 *
 * @param sessionId     the session id (UUID / Bearer token)
 * @param accountStatus the owning account's status, or {@code null} for a bare anonymous session
 * @param identityId    the active identity (base-36 Snowflake), or {@code null} if none selected
 * @param loggedIn      true for a password-login session
 */
@Schema(name = "AuthSessionResponse", description = "Current session snapshot")
public record AuthSessionResponse(
  @Schema(description = "Session id (UUID)")
  String sessionId,

  @Schema(description = "Owning account status", nullable = true, example = "ACTIVE")
  String accountStatus,

  @JsonSerialize(using = SnowflakeBase36Serializer.class)
  @Schema(description = "Active identity id (base-36 Snowflake)", nullable = true)
  Long identityId,

  @Schema(description = "True for a password-login session")
  boolean loggedIn
) {
}
