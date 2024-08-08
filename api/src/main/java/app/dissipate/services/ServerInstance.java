package app.dissipate.services;

import app.dissipate.data.models.Server;
import app.dissipate.data.models.dto.MaxIntDto;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.core.runtime.context.VertxContextSafetyToggle;
import io.smallrye.common.vertx.VertxContext;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import io.quarkus.scheduler.Scheduled;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.logging.Logger;

import static app.dissipate.data.models.Server.markAbandonedServersAsShutdown;

@ApplicationScoped
public class ServerInstance {
  private static final org.jboss.logging.Logger LOGGER = org.jboss.logging.Logger.getLogger(ServerInstance.class);

  Server server;

  @Inject
  Mutiny.SessionFactory factory;

  @Produces
  public Server getServer() {
    return server;
  }

  @WithSpan("ServerInstance.onStart")
  public void onStart(@Observes StartupEvent event, Vertx vertx, Mutiny.SessionFactory factory) {
    // We need a duplicated vertx context for hibernate reactive
    Context context = VertxContext.getOrCreateDuplicatedContext(vertx);
    // Don't forget to mark the context safe
    VertxContextSafetyToggle.setContextSafe(context, true);
    // Run the logic on the context created above
    context.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void event) {
        // We cannot use the Panache.withTransaction() and friends because the CDI request context is not active/propagated
        factory.withTransaction(session ->
            Server.findMaxInstanceId().call(maxIntDto -> {
              if (maxIntDto == null || maxIntDto.maxValue < 1000) {
                return createNewServer(maxIntDto);
              } else {
                return findFirstUnusedServer();
              }
            })
          )
          // We need to subscribe to the Uni to trigger the action
          .subscribe().with(v -> {
          });

        factory.withTransaction(session -> {
          Uni<Integer> result = markAbandonedServersAsShutdown();
          if (result == null) {
            LOGGER.error("markAbandonedServersAsShutdown returned null.");
            return Uni.createFrom().failure(new NullPointerException("markAbandonedServersAsShutdown returned null."));
          }
          return result;
        }).subscribe().with(v -> {
          LOGGER.info("Second transaction completed successfully.");
        }, failure -> {
          LOGGER.error("Second transaction failed.", failure);
        });
      }
    });
  }

  @Scheduled(every = "30s")
  Uni<Void> heartBeat() {
    Context context = Vertx.currentContext();
    // Don't forget to mark the context safe
    VertxContextSafetyToggle.setContextSafe(context, true);

    // Run the logic on the context created above
    context.runOnContext(event -> factory.withTransaction(session -> {
      if (server != null) {
        return Server.byId(server.id).onItem().call(s -> {
          if (s != null) {
            s.seen = LocalDateTime.now();
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
    server.seen = LocalDateTime.now();
    server.isShutdown = false;
    return server.persistAndFlush();
  }

  private Uni<Server> findFirstUnusedServer() {
    return Server.findFirstUnusedServer().call(s -> {
      if (s == null) {
        return Uni.createFrom().failure(new RuntimeException("No server instance available"));
      }
      server = s;
      server.seen = LocalDateTime.now();
      server.isShutdown = false;
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
          return s.persistAndFlush();
        })
      )
      // Subscribe to the Uni to trigger the action
      .subscribe().with(v -> {
      });
  }
}
