package app.dissipate.services;

import app.dissipate.api.websocket.WebSocketSessionManager;
import app.dissipate.data.models.ChatParticipant;
import app.dissipate.data.models.Server;
import app.dissipate.data.models.Session;
import app.dissipate.grpc.internal.NotifyChatUpdateRequest;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class ChatNotificationService {

  private static final Logger LOGGER = Logger.getLogger(ChatNotificationService.class);

  public static final String CHAT_UPDATE_EVENT = "chat-update";

  @Inject
  EventBus bus;

  @Inject
  @Named("currentServer")
  Server currentServer;

  @Inject
  WebSocketSessionManager webSocketSessionManager;

  @Inject
  NodeConnectionManager nodeConnectionManager;

  public void fireNotification(Long chatId, Long senderIdentityId, Long eventId) {
    String payload = chatId + "|" + senderIdentityId + "|" + eventId;
    bus.publish(CHAT_UPDATE_EVENT, payload);
  }

  @WithSession
  @ConsumeEvent(CHAT_UPDATE_EVENT)
  @WithSpan("ChatNotificationService.handleChatUpdate")
  public Uni<Void> handleChatUpdate(String payload) {
    String[] parts = payload.split("\\|", 3);
    if (parts.length != 3) {
      LOGGER.errorv("Invalid chat update payload: {0}", payload);
      return Uni.createFrom().voidItem();
    }

    Long chatId;
    Long senderIdentityId;
    Long eventId;
    try {
      chatId = Long.parseLong(parts[0]);
      senderIdentityId = Long.parseLong(parts[1]);
      eventId = Long.parseLong(parts[2]);
    } catch (NumberFormatException e) {
      LOGGER.errorv("Invalid chat update payload (non-numeric id): {0}", payload);
      return Uni.createFrom().voidItem();
    }

    LOGGER.debugv("Processing chat notification for chat {0}, event {1}", chatId, eventId);

    return ChatParticipant.findOtherParticipants(chatId, senderIdentityId)
      .onItem().transformToUni(participants -> {
        if (participants.isEmpty()) {
          LOGGER.debugv("No other participants in chat {0}", chatId);
          return Uni.createFrom().voidItem();
        }

        List<Long> identityIds = participants.stream()
          .map(p -> p.identity.id)
          .toList();

        return Session.findConnectedByIdentityIds(identityIds)
          .onItem().transformToUni(sessions -> {
            if (sessions.isEmpty()) {
              LOGGER.debugv("No connected sessions for chat {0} participants", chatId);
              return Uni.createFrom().voidItem();
            }

            return dispatchToNodes(sessions, chatId, senderIdentityId, eventId);
          });
      });
  }

  private Uni<Void> dispatchToNodes(List<Session> sessions, Long chatId, Long senderIdentityId, Long eventId) {
    Map<Long, List<Session>> sessionsByServer = sessions.stream()
      .collect(Collectors.groupingBy(s -> s.connectedServer.id));

    List<Uni<Void>> dispatches = sessionsByServer.entrySet().stream()
      .map(entry -> {
        Long serverId = entry.getKey();
        List<String> sessionIds = entry.getValue().stream()
          .map(s -> s.id.toString())
          .toList();

        if (serverId.equals(currentServer.id)) {
          return dispatchLocal(sessionIds, chatId, senderIdentityId, eventId);
        } else {
          Server targetServer = entry.getValue().get(0).connectedServer;
          return dispatchRemote(targetServer, sessionIds, chatId, senderIdentityId, eventId);
        }
      })
      .toList();

    return Uni.combine().all().unis(dispatches).discardItems();
  }

  private Uni<Void> dispatchLocal(List<String> sessionIds, Long chatId, Long senderIdentityId, Long eventId) {
    LOGGER.debugv("Dispatching chat update locally to {0} sessions", sessionIds.size());
    String jsonPayload = buildJsonPayload(chatId, senderIdentityId, eventId);
    return webSocketSessionManager.sendToLocalSessions(sessionIds, jsonPayload);
  }

  private Uni<Void> dispatchRemote(Server targetServer, List<String> sessionIds, Long chatId, Long senderIdentityId, Long eventId) {
    LOGGER.debugv("Dispatching chat update to remote server {0} for {1} sessions", targetServer.id, sessionIds.size());

    NotifyChatUpdateRequest request = NotifyChatUpdateRequest.newBuilder()
      .setServerToken(targetServer.token)
      .setChatId(toBase36(chatId))
      .setSenderIdentityId(toBase36(senderIdentityId))
      .addAllSessionIds(sessionIds)
      .setTriggeringEventId(toBase36(eventId))
      .build();

    return nodeConnectionManager.sendNotification(targetServer, request)
      .replaceWithVoid()
      .onFailure().recoverWithUni(t -> {
        LOGGER.errorv("Failed to dispatch to server {0}: {1}", targetServer.id, t.getMessage());
        return Uni.createFrom().voidItem();
      });
  }

  private String buildJsonPayload(Long chatId, Long senderIdentityId, Long eventId) {
    return "{\"type\":\"chat_update\""
      + ",\"chatId\":\"" + toBase36(chatId) + "\""
      + ",\"senderIdentityId\":\"" + toBase36(senderIdentityId) + "\""
      + ",\"eventId\":\"" + toBase36(eventId) + "\""
      + "}";
  }

  private static String toBase36(Long id) {
    return id == null ? "" : Long.toString(id, 36);
  }
}
