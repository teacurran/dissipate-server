package app.dissipate.data.models;

public enum AccountStatus {
  PENDING,
  ACTIVE,
  DISABLED,
  SUSPENDED,
  BANNED;

  private AccountStatus() {
  }

  public static AccountStatus fromValue(String id) {
    return AccountStatus.valueOf(id);
  }
}
