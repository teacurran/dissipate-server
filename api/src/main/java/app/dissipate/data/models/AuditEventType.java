package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Types of audited events. Persisted as a string (no DB CHECK constraint), so this enum is the
 * source of truth and may be extended freely as features land. Grouped by subsystem.
 */
@RegisterForReflection
public enum AuditEventType {
  // Auth & registration
  AUTH_REGISTER,
  AUTH_VERIFY,
  AUTH_LOGIN,
  AUTH_LOGIN_FAILED,
  AUTH_LOGOUT,
  AUTH_OTP_RESEND,
  AUTH_LOCKOUT,
  AUTH_PASSWORD_SET,
  AUTH_PASSWORD_CHANGE,
  AUTH_PASSWORD_RESET_REQUEST,
  AUTH_PASSWORD_RESET_CONFIRM,

  // Sessions
  SESSION_REVOKE,

  // Identities
  IDENTITY_CREATE,
  IDENTITY_UPDATE,
  IDENTITY_DELETE,
  IDENTITY_SWITCH,
  IDENTITY_AVATAR_UPDATE,

  // Social graph
  FOLLOW,
  UNFOLLOW,
  BLOCK,
  UNBLOCK
}
