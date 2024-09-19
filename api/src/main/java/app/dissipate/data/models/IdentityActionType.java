package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum IdentityActionType {
  FOLLOW,
  UNFOLLOW,
  BLOCK,
  UNBLOCK,
  MUTE,
  UNMUTE,
  POST_CREATE,
  POST_EDIT,
  POST_DELETE
}
