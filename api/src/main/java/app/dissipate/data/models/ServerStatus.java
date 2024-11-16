package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum ServerStatus {
  UNKNOWN,
  ACTIVE,
  ABANDONED,
  SHUTDOWN;

  ServerStatus() {
  }

  public static ServerStatus fromValue(String id) {
    return ServerStatus.valueOf(id);
  }
}
