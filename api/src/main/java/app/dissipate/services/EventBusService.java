package app.dissipate.services;

import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EventBusService {
  @Inject
  EventBus bus;

  public void publish(String string, String message) {
    bus.publish(string, message);
  }

}
