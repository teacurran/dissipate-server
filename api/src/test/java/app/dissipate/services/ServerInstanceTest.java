package app.dissipate.services;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.test.InjectMock;
// import io.quarkus.test.junit.QuarkusTest; // Disabled - requires database setup
import io.vertx.core.Vertx;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// @QuarkusTest // Disabled - requires database setup
public class ServerInstanceTest {

  @InjectMock
  ServerInstance serverInstance;

  @Inject
  Mutiny.SessionFactory factory;

  @Inject
  Vertx vertx;

  @Test
  @Disabled("Requires Quarkus context with database - needs proper test setup")
  void testOnStart() {
    // this is a stub, fill it out

    // Arrange
    StartupEvent event = mock(StartupEvent.class);

    // Act
    serverInstance.onStart(event, vertx, factory);

    // Assert
    verify(serverInstance, times(1)).onStart(event, vertx, factory);
  }
}
