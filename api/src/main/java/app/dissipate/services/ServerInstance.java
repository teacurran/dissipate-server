package app.dissipate.services;

import app.dissipate.data.models.Server;
import app.dissipate.data.models.dto.MaxIntDto;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import java.time.Duration;
import java.time.LocalDateTime;

@ApplicationScoped
public class ServerInstance {

    Duration DEFAULT_DB_WAIT = Duration.ofSeconds(10);

    Server server;

    @Produces
    public Server getServer() {
        return server;
    }

    @WithSpan("ServerInstance.onStart")
    public void onStart(@Observes StartupEvent event) {
        MaxIntDto maxIntDto = Server.findMaxInstanceId().await().atMost(DEFAULT_DB_WAIT);
        if (maxIntDto == null) {
            server = new Server();
            server.instanceNumber = 1;
        } else {
            if (maxIntDto.maxValue < 1000) {
                server = new Server();
                server.instanceNumber = maxIntDto.maxValue + 1;
            } else {
                server = Server.findFirstUnusedServer().await().atMost(DEFAULT_DB_WAIT);
            }
        }

        if (server == null) {
            throw new RuntimeException("No server instance available");
        }
        server.seen = LocalDateTime.now();
        server.isShutdown = false;
        server.persistAndFlush().await().atMost(DEFAULT_DB_WAIT);
    }

    @WithSpan("ServerInstance.onStop")
    public void onStop(@Observes ShutdownEvent event) {
        if (server != null) {
            server = Server.byId(server.id).await().atMost(DEFAULT_DB_WAIT);
            server.isShutdown = true;
            server.persistAndFlush().await().atMost(DEFAULT_DB_WAIT);
        }
    }
}