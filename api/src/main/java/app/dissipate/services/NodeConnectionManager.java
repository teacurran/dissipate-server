package app.dissipate.services;

import app.dissipate.data.models.Server;
import app.dissipate.grpc.internal.MutinyDissipateInternalServiceGrpc;
import app.dissipate.grpc.internal.NotifyChatUpdateRequest;
import app.dissipate.grpc.internal.NotifyChatUpdateResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.quarkus.runtime.ShutdownEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class NodeConnectionManager {

  private static final Logger LOGGER = Logger.getLogger(NodeConnectionManager.class);

  private final ConcurrentHashMap<Long, ClientEntry> clients = new ConcurrentHashMap<>();

  public Uni<NotifyChatUpdateResponse> sendNotification(Server server, NotifyChatUpdateRequest request) {
    var stub = getStub(server);
    return stub.notifyChatUpdate(request)
      .onFailure().invoke(t -> {
        LOGGER.warnv("Failed to send notification to server {0}: {1}", server.id, t.getMessage());
        evict(server.id);
      });
  }

  private MutinyDissipateInternalServiceGrpc.MutinyDissipateInternalServiceStub getStub(Server server) {
    ClientEntry entry = clients.computeIfAbsent(server.id, id -> {
      String target = server.hostname + ":" + server.port;
      LOGGER.infov("Creating gRPC channel to peer node {0} at {1}", id, target);
      ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
        .usePlaintext()
        .build();
      var stub = MutinyDissipateInternalServiceGrpc.newMutinyStub(channel);
      return new ClientEntry(channel, stub);
    });
    return entry.stub;
  }

  public void evict(Long serverId) {
    ClientEntry entry = clients.remove(serverId);
    if (entry != null) {
      LOGGER.infov("Evicting gRPC channel for server {0}", serverId);
      entry.channel.shutdown();
    }
  }

  void onShutdown(@Observes ShutdownEvent event) {
    LOGGER.info("Shutting down all peer gRPC channels");
    clients.forEach((id, entry) -> {
      try {
        entry.channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        entry.channel.shutdownNow();
        Thread.currentThread().interrupt();
      }
    });
    clients.clear();
  }

  private record ClientEntry(
    ManagedChannel channel,
    MutinyDissipateInternalServiceGrpc.MutinyDissipateInternalServiceStub stub
  ) {}
}
