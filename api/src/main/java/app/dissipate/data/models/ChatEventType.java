package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum ChatEventType {
  MESSAGE,
  JOIN,
  LEAVE,
  KICK,
  BAN,
  UNBAN,
  MUTE,
  UNMUTE,
  PROMOTE,
  DEMOTE
}
