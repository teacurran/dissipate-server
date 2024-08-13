package app.dissipate.data.models;

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
