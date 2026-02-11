package app.dissipate.api.websocket;

import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.PathParam;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@WebSocket(path = "/ws/chat/{sessionId}")
public class ChatWebSocket {

  private static final Logger LOGGER = Logger.getLogger(ChatWebSocket.class);

  @Inject
  WebSocketConnection connection;

  @Inject
  WebSocketSessionManager sessionManager;

  @OnOpen
  public Uni<Void> onOpen(@PathParam String sessionId) {
    LOGGER.debugv("WebSocket opened for session {0}", sessionId);
    return sessionManager.registerConnection(sessionId, connection);
  }

  @OnClose
  public Uni<Void> onClose() {
    String sessionId = connection.pathParam("sessionId");
    LOGGER.debugv("WebSocket closed for session {0}", sessionId);
    return sessionManager.unregisterConnection(sessionId);
  }

  @OnTextMessage
  public void onMessage(String message) {
    // Push-only channel for now; client messages can be used later
    // for typing indicators, acks, etc.
  }
}
