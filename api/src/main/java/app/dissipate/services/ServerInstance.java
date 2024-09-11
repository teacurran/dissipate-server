package app.dissipate.services;

import app.dissipate.data.models.Server;
import app.dissipate.data.models.ServerStatus;
import app.dissipate.data.models.dto.MaxIntDto;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.vertx.core.runtime.context.VertxContextSafetyToggle;
import io.quarkus.vertx.http.runtime.HttpConfiguration;
import io.smallrye.common.vertx.VertxContext;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static app.dissipate.data.models.Server.markAbandonedServersAsShutdown;

@ApplicationScoped
public class ServerInstance {
  private static final org.jboss.logging.Logger LOGGER = org.jboss.logging.Logger.getLogger(ServerInstance.class);

  Server server;

  @Inject
  Mutiny.SessionFactory factory;

  @Inject
  ZoneOffset zoneOffset;

  @Inject
  HttpConfiguration httpConfiguration;

  @Produces
  @Named("currentServer")
  public Server getCurrentServer() {
    return server;
  }

  @WithSpan("ServerInstance.onStart")
  public void onStart(@Observes StartupEvent event, Vertx vertx, Mutiny.SessionFactory factory) {
    // duplicate vertx context for hibernate reactive
    Context context = VertxContext.getOrCreateDuplicatedContext(vertx);
    // mark the context safe
    VertxContextSafetyToggle.setContextSafe(context, true);
    // Run the logic on the context created above
    context.runOnContext(event1 -> {
      // We cannot use the Panache.withTransaction() and friends because the CDI request context is not active yet
      factory.withTransaction(session ->
          Server.findMaxInstanceId().call(maxIntDto -> {
            if (maxIntDto == null || maxIntDto.maxValue < 1000) {
              return createNewServer(maxIntDto);
            } else {
              return findFirstUnusedServer();
            }
          }).onItem().transformToUni(s -> {
            LOGGER.info("Server instance started: " + s.id);
            return Uni.createFrom().nullItem();
          })
        )
        // We need to subscribe to the Uni to trigger the action
        .subscribe().with(v -> {
        });
    });
  }

  @Scheduled(every = "1h")
  @WithSpan("ServerInstance.markAbandonedServersAsShutdown")
  Uni<Void> markAbandoned() {
    Context context = Vertx.currentContext();
    // Don't forget to mark the context safe
    VertxContextSafetyToggle.setContextSafe(context, true);

    // Run the logic on the context created above
    context.runOnContext(event -> factory.withTransaction(session -> {
      Uni<Integer> result = markAbandonedServersAsShutdown(zoneOffset);
      if (result == null) {
        LOGGER.error("markAbandonedServersAsShutdown returned null.");
        return Uni.createFrom().failure(new NullPointerException("markAbandonedServersAsShutdown returned null."));
      }
      return result;
    }).subscribe().with(v -> {
      LOGGER.info("marked " + v + " servers as abandoned.");
    }, failure -> {
      LOGGER.error("Second transaction failed.", failure);
    }));

    return Uni.createFrom().nullItem();
  }


  @Scheduled(every = "30s")
  @WithSpan("ServerInstance.heartbeat")
  Uni<Void> heartBeat() {
    Context context = Vertx.currentContext();
    // Don't forget to mark the context safe
    VertxContextSafetyToggle.setContextSafe(context, true);

    // Run the logic on the context created above
    context.runOnContext(event -> factory.withTransaction(session -> {
      if (server != null) {
        return Server.byId(server.id).onItem().call(s -> {
          if (s != null) {
            s.seen = LocalDateTime.now().toInstant(zoneOffset);
            return s.persistAndFlush();
          }
          return null;
        }).onFailure().transform(t -> {
          // Log the error
          LOGGER.error("Error occurred", t);
          return null;
        });
      }
      return null;
    }).subscribe().with(v -> {
    }));

//    if (server != null) {
//      Server.byId(server.id).onItem().ifNotNull().call(s -> {
//        s.seen = LocalDateTime.now();
//        return s.persistAndFlush();
//      });
//    }
    return Uni.createFrom().nullItem();
  }

  private Uni<Server> createNewServer(MaxIntDto maxIntDto) {
    server = new Server();
    server.instanceNumber = maxIntDto == null ? 1 : maxIntDto.maxValue + 1;
    server.launched = LocalDateTime.now().toInstant(zoneOffset);
    server.seen = LocalDateTime.now().toInstant(zoneOffset);
    server.status = ServerStatus.ACTIVE;
    server.hostname = httpConfiguration.host;
    server.port = httpConfiguration.port;
    server.isShutdown = false;
    server.token = UUID.randomUUID().toString();
    return server.persistAndFlush();
  }

  private Uni<Server> findFirstUnusedServer() {
    return Server.findFirstUnusedServer().call(s -> {
      if (s == null) {
        return Uni.createFrom().failure(new RuntimeException("No server instance available"));
      }
      server = s;
      server.launched = LocalDateTime.now().toInstant(zoneOffset);
      server.seen = LocalDateTime.now().toInstant(zoneOffset);
      server.status = ServerStatus.ACTIVE;
      server.hostname = httpConfiguration.host;
      server.port = httpConfiguration.port;
      server.isShutdown = false;
      server.token = UUID.randomUUID().toString();
      return server.persistAndFlush();
    });
  }

  @WithSpan("ServerInstance.onStop")
  public void onStop(@Observes ShutdownEvent event, Vertx vertx, Mutiny.SessionFactory factory) {
    if (server != null) {
      // Create a new Vertx context for Hibernate Reactive
      Context context = VertxContext.getOrCreateDuplicatedContext(vertx);
      // Mark the context as safe
      VertxContextSafetyToggle.setContextSafe(context, true);
      // Run the logic on the created context
      context.runOnContext(v -> handleStop(factory));
    }
  }

  private void handleStop(Mutiny.SessionFactory factory) {
    // Start a new transaction
    factory.withTransaction(session ->
        // Find the server by ID
        Server.byId(server.id).call(s -> {
          s.isShutdown = true;
          s.status = ServerStatus.SHUTDOWN;
          return s.persistAndFlush();
        })
      )
      // Subscribe to the Uni to trigger the action
      .subscribe().with(v -> {
      });
  }
}
