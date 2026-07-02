package app.dissipate.api.rest;

import app.dissipate.api.rest.auth.CurrentSession;
import app.dissipate.api.rest.auth.RestAuthenticated;
import app.dissipate.api.rest.dto.SendMessageResponse;
import app.dissipate.api.rest.error.RestApiException;
import app.dissipate.api.rest.error.RestErrorCodes;
import app.dissipate.data.jpa.UuidGenerator;
import app.dissipate.data.models.Chat;
import app.dissipate.data.models.ChatEvent;
import app.dissipate.data.models.ChatEventType;
import app.dissipate.services.ChatNotificationService;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/api/chat")
public class ChatResource {

  @Inject
  ChatNotificationService chatNotificationService;

  @Inject
  UuidGenerator uuidGenerator;

  @Inject
  CurrentSession currentSession;

  @POST
  @Path("/{chatId}/messages")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RestAuthenticated
  @WithTransaction
  public Uni<Response> sendMessage(
    @PathParam("chatId") UUID chatId,
    @Valid SendMessageRequest body
  ) {
    return currentSession.requireIdentity().onItem().transformToUni(identity ->
      Chat.<Chat>findById(chatId).onItem().transformToUni(chat -> {
        if (chat == null) {
          throw new RestApiException(Response.Status.NOT_FOUND, RestErrorCodes.NOT_FOUND);
        }

        ChatEvent event = new ChatEvent();
        event.id = uuidGenerator.generate();
        event.chat = chat;
        event.identity = identity;
        event.type = ChatEventType.MESSAGE;
        event.message = body.message();

        return event.<ChatEvent>persistAndFlush()
          .onItem().invoke(e ->
            chatNotificationService.fireNotification(chatId, identity.id, e.id))
          .onItem().transform(e ->
            Response.ok(new SendMessageResponse(e.id)).build());
      }));
  }

  public record SendMessageRequest(@NotBlank String message) {}
}
