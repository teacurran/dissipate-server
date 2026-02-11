package app.dissipate.api.rest;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.models.Chat;
import app.dissipate.data.models.ChatEvent;
import app.dissipate.data.models.ChatEventType;
import app.dissipate.data.models.Session;
import app.dissipate.services.ChatNotificationService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@Path("/api/chat")
public class ChatResource {

  private static final Logger LOGGER = Logger.getLogger(ChatResource.class);

  @Inject
  ChatNotificationService chatNotificationService;

  @Inject
  SnowflakeIdGenerator snowflakeIdGenerator;

  @POST
  @Path("/{chatId}/messages")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @WithTransaction
  public Uni<Response> sendMessage(
    @PathParam("chatId") String chatId,
    @HeaderParam("Authorization") String authorization,
    SendMessageRequest body
  ) {
    if (authorization == null || authorization.isBlank()) {
      return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED).build());
    }

    String sessionId = authorization.startsWith("Bearer ")
      ? authorization.substring(7).trim()
      : authorization.trim();

    return Session.findBySidValidated(sessionId)
      .onItem().ifNull().continueWith(() -> {
        return null;
      })
      .onItem().transformToUni(session -> {
        if (session == null) {
          return Uni.createFrom().item(
            Response.status(Response.Status.UNAUTHORIZED)
              .entity("{\"error\":\"Invalid session\"}")
              .build()
          );
        }

        if (session.identity == null) {
          return Uni.createFrom().item(
            Response.status(Response.Status.FORBIDDEN)
              .entity("{\"error\":\"No identity selected\"}")
              .build()
          );
        }

        return Chat.<Chat>findById(chatId)
          .onItem().transformToUni(chat -> {
            if (chat == null) {
              return Uni.createFrom().item(
                Response.status(Response.Status.NOT_FOUND)
                  .entity("{\"error\":\"Chat not found\"}")
                  .build()
              );
            }

            ChatEvent event = new ChatEvent();
            event.id = snowflakeIdGenerator.generate("ChatEvent");
            event.chat = chat;
            event.identity = session.identity;
            event.type = ChatEventType.MESSAGE;
            event.message = body.message();

            return event.<ChatEvent>persistAndFlush()
              .onItem().invoke(e ->
                chatNotificationService.fireNotification(chatId, session.identity.id, e.id)
              )
              .onItem().transform(e ->
                Response.ok("{\"eventId\":\"" + e.id + "\"}").build()
              );
          });
      });
  }

  public record SendMessageRequest(String message) {}
}
