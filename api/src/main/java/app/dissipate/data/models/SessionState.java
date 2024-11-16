package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum SessionState {
  ACTIVE,
  INACTIVE,
  EXPIRED,
  NEEDS_VALIDATION
}
