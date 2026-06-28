package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

/** Lifecycle of a registered third-party API application. */
@RegisterForReflection
public enum ApiAppStatus {
  ACTIVE,
  DISABLED;

  ApiAppStatus() {
  }
}
