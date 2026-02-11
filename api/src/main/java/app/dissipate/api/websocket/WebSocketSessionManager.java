package app.dissipate.api.websocket;

import app.dissipate.data.models.Server;
import app.dissipate.data.models.Session;
import io.quarkus.websockets.next.WebSocketConnection;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class WebSocketSessionManager {

  private static final Logger LOGGER = Logger.getLogger(WebSocketSessionManager.class);

  private final ConcurrentHashMap<String, WebSocketConnection> connections = new ConcurrentHashMap<>();

  @Inject
  Mutiny.SessionFactory factory;

  @Inject
  @Named("currentServer")
  Server currentServer;

  public Uni<Void> registerConnection(String sessionId, WebSocketConnection connection) {
    return factory.withTransaction(s ->
      Session.<Session>findById(UUID.fromString(sessionId))
        .onItem().ifNotNull().invoke(session -> {
          session.connectedServer = currentServer;
          connections.put(sessionId, connection);
          LOGGER.debugv("Registered WebSocket connection for session {0}", sessionId);
        })
        .onItem().ifNull().failWith(() ->
          new IllegalArgumentException("Session not found: " + sessionId))
        .replaceWithVoid()
    );
  }

  public Uni<Void> unregisterConnection(String sessionId) {
    connections.remove(sessionId);
    return factory.withTransaction(s ->
      Session.<Session>findById(UUID.fromString(sessionId))
        .onItem().ifNotNull().invoke(session -> {
          session.connectedServer = null;
          LOGGER.debugv("Unregistered WebSocket connection for session {0}", sessionId);
        })
        .replaceWithVoid()
    );
  }

  public Uni<Void> sendToLocalSessions(List<String> sessionIds, String jsonPayload) {
    List<Uni<Void>> sends = sessionIds.stream()
      .map(sid -> {
        WebSocketConnection conn = connections.get(sid);
        if (conn != null) {
          return conn.sendText(jsonPayload);
        }
        LOGGER.debugv("No local connection for session {0}, skipping", sid);
        return Uni.createFrom().<Void>voidItem();
      })
      .toList();

    if (sends.isEmpty()) {
      return Uni.createFrom().voidItem();
    }

    return Uni.combine().all().unis(sends).discardItems();
  }
}
