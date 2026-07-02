package app.dissipate.api.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.UUID;

/**
 * Response for a posted chat message.
 *
 * @param eventId the created {@code ChatEvent} id
 */
@Schema(name = "SendMessageResponse", description = "Result of posting a chat message")
public record SendMessageResponse(
  @Schema(description = "Created chat event id", example = "018f0e2a-9c1b-7a3e-8f21-3b6d5e0a9c1b")
  UUID eventId
) {
}
