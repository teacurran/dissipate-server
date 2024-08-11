package app.dissipate.data.models;

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
