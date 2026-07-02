package app.dissipate.data.models;

import java.util.Map;
import java.util.UUID;

/**
 * An entity whose changes are recorded as {@link EntityRevision}s. The auditable state is provided
 * explicitly via {@link #auditSnapshot()} rather than reflectively so that lazy associations are not
 * touched (they would fail under Hibernate Reactive) and encrypted PII can be redacted or omitted.
 * Federated (publishable) entities also expose a stable global handle + origin region so their
 * revisions remain meaningful across regions; source-of-truth entities return null for those.
 */
public interface Auditable {

  /** Stable type name recorded in the revision log, e.g. {@code "Post"}. */
  String entityType();

  /** The entity's local Snowflake id. */
  Long entityId();

  /** The stable federation handle, or null for source-of-truth (non-published) entities. */
  UUID auditGlobalId();

  /** The origin region, or null for source-of-truth entities. */
  Integer auditOriginRegion();

  /**
   * The auditable state as a stable field-name → value map. Exclude lazy associations (reference
   * them by id instead) and raw encrypted PII. Used for the snapshot and the reconciliation hash.
   */
  Map<String, Object> auditSnapshot();
}
