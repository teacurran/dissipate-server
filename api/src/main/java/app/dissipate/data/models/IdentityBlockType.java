package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum IdentityBlockType {
  BLOCKED,
  IGNORED
}
