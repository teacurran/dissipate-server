package app.dissipate.api.rest.dto;

import app.dissipate.utils.SnowflakeBase36Serializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Response for a posted chat message.
 *
 * @param eventId the created {@code ChatEvent} id, rendered as a base-36 Snowflake string
 */
@Schema(name = "SendMessageResponse", description = "Result of posting a chat message")
public record SendMessageResponse(
  @JsonSerialize(using = SnowflakeBase36Serializer.class)
  @Schema(description = "Created chat event id (base-36 Snowflake)", example = "1z9k3p")
  Long eventId
) {
}
