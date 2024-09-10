package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum ProductType {
  SUBSCRIPTION,
  ONE_TIME_PURCHASE
}
