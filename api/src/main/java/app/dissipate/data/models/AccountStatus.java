package app.dissipate.data.models;

public enum AccountStatus {
  PENDING,
  ACTIVE,
  DISABLED,
  SUSPENDED,
  BANNED;

  AccountStatus() {
  }

  public static AccountStatus fromValue(String id) {
    return AccountStatus.valueOf(id);
  }
}
