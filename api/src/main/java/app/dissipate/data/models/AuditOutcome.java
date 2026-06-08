package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

/** Result of an audited action. */
@RegisterForReflection
public enum AuditOutcome {
  SUCCESS,
  FAILURE
}
