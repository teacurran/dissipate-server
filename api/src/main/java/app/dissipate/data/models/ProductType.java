package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum ProductType {
  SUBSCRIPTION,
  DIGITAL_ASSET,
  PHYSICAL_ITEM,
  SERVICE,
  BUNDLE,
  ALBUM,
  TIP
}
