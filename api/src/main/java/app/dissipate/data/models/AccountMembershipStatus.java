package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
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
