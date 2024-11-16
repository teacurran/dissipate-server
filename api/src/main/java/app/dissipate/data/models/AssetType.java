package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum AssetType {
  TEXT,
  IMAGE,
  VIDEO;

  AssetType() {
  }

  public static AssetType fromValue(String value) {
    return AssetType.valueOf(value);
  }
}
