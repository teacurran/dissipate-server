package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

/** The kind of change a {@link EntityRevision} records. */
@RegisterForReflection
public enum RevisionOp {
  INSERT,
  UPDATE,
  DELETE
}
