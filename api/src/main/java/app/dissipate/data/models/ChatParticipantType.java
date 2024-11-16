package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum ChatParticipantType {
  PARTICIPANT,
  MODERATOR,
  OWNER
}
