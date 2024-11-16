package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum AccountStatus {
  ANONYMOUS,
  ACTIVE,
  DISABLED,
  SUSPENDED,
  BANNED;

  AccountStatus() {
  }
}
