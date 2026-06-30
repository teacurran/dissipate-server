package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * The kind of authenticated caller a usage counter is attributed to: a first-party {@code USER}
 * (keyed by account id) or a third-party {@code APP} (keyed by app id).
 */
@RegisterForReflection
public enum PrincipalKind {
  USER,
  APP
}
