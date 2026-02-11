package app.dissipate.api.grpc.internal;

import app.dissipate.api.websocket.WebSocketSessionManager;
import app.dissipate.data.models.Server;
import app.dissipate.grpc.internal.DissipateInternalService;
import app.dissipate.grpc.internal.NotifyChatUpdateRequest;
import app.dissipate.grpc.internal.NotifyChatUpdateResponse;
import app.dissipate.grpc.internal.PingRequest;
import app.dissipate.grpc.internal.PingResponse;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;

@GrpcService
public class DissipateInternalServiceImpl implements DissipateInternalService {

  private static final Logger LOGGER = Logger.getLogger(DissipateInternalServiceImpl.class);

  @Inject
  WebSocketSessionManager webSocketSessionManager;

  @Inject
  @Named("currentServer")
  Server currentServer;

  private final Instant startedAt = Instant.now();

  @Override
  public Uni<NotifyChatUpdateResponse> notifyChatUpdate(NotifyChatUpdateRequest request) {
    if (!currentServer.token.equals(request.getServerToken())) {
      LOGGER.warnv("Rejected internal gRPC call with invalid token for chat {0}", request.getChatId());
      return Uni.createFrom().item(
        NotifyChatUpdateResponse.newBuilder().setDeliveredCount(0).build()
      );
    }

    String jsonPayload = buildJsonPayload(request);
    return webSocketSessionManager.sendToLocalSessions(request.getSessionIdsList(), jsonPayload)
      .replaceWith(
        NotifyChatUpdateResponse.newBuilder()
          .setDeliveredCount(request.getSessionIdsCount())
          .build()
      );
  }

  @Override
  public Uni<PingResponse> ping(PingRequest request) {
    long uptimeSeconds = Duration.between(startedAt, Instant.now()).getSeconds();
    return Uni.createFrom().item(
      PingResponse.newBuilder()
        .setServerId(currentServer.id)
        .setUptimeSeconds(uptimeSeconds)
        .build()
    );
  }

  private String buildJsonPayload(NotifyChatUpdateRequest request) {
    return "{\"type\":\"chat_update\""
      + ",\"chatId\":\"" + escapeJson(request.getChatId()) + "\""
      + ",\"senderIdentityId\":\"" + escapeJson(request.getSenderIdentityId()) + "\""
      + ",\"eventId\":\"" + escapeJson(request.getTriggeringEventId()) + "\""
      + "}";
  }

  private static String escapeJson(String value) {
    if (value == null) return "";
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
