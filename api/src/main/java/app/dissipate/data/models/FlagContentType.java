package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum FlagContentType {
  IDENTITY,
  POST,
  CHAT_EVENT
}
