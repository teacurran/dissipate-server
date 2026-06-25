package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * First-party end-user role. Hierarchical: {@code ADMIN} satisfies {@code VERIFIED}
 * which satisfies {@code USER}. Maps 1:1 (minus the proto zero-value) to the gRPC
 * {@code dissipate.v1.Role} enum and is enforced against a method's
 * {@code MethodPolicy.min_role} by the auth pipeline.
 */
@RegisterForReflection
public enum AccountRole {
  USER,
  VERIFIED,
  ADMIN;

  /**
   * True when this role meets or exceeds {@code minimum} in the hierarchy
   * (e.g. {@code ADMIN.satisfies(USER)} is true).
   */
  public boolean satisfies(AccountRole minimum) {
    return this.ordinal() >= minimum.ordinal();
  }
}
