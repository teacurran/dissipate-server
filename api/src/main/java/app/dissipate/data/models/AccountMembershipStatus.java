package app.dissipate.data.models;

public enum AccountMembershipStatus {
  ACTIVE,
  PAUSED,
  CANCELLED,
  EXPIRED;

  AccountMembershipStatus() {
  }

  public static AccountMembershipStatus fromValue(String id) {
    return AccountMembershipStatus.valueOf(id);
  }
}