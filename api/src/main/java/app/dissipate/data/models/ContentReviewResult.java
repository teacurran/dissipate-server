package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum ContentReviewResult {
  TERMS_VIOLATION,
  TERMS_VIOLATION_ADULT,
  TERMS_VIOLATION_HATE,
  TERMS_VIOLATION_VIOLENCE,
  TERMS_VIOLATION_SPAM,
  TERMS_VIOLATION_OTHER,
  DOES_NOT_VIOLATE_TERMS
}
