package app.dissipate.data.models;

import app.dissipate.utils.PageCursor;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "identity_organizations")
@NamedQuery(name = IdentityOrganization.QUERY_BY_ORGANIZATION, query = """
    SELECT io
    FROM IdentityOrganization io
    WHERE io.organization.id = :organizationId
      AND (:cursorCreated IS NULL OR io.created < :cursorCreated
        OR (io.created = :cursorCreated AND io.id < :cursorId))
    ORDER BY io.created DESC, io.id DESC
  """)
@NamedQuery(name = IdentityOrganization.QUERY_BY_IDENTITY, query = """
    SELECT io
    FROM IdentityOrganization io
    WHERE io.identity.id = :identityId
      AND (:cursorCreated IS NULL OR io.created < :cursorCreated
        OR (io.created = :cursorCreated AND io.id < :cursorId))
    ORDER BY io.created DESC, io.id DESC
  """)
@NamedQuery(name = IdentityOrganization.QUERY_APPROVED_BY_IDENTITY, query = """
    SELECT io
    FROM IdentityOrganization io
    WHERE io.approvedBy.id = :identityId
      AND (:cursorCreated IS NULL OR io.created < :cursorCreated
        OR (io.created = :cursorCreated AND io.id < :cursorId))
    ORDER BY io.created DESC, io.id DESC
  """)
public class IdentityOrganization extends DefaultPanacheEntityWithTimestamps {

  public static final String QUERY_BY_ORGANIZATION = "IdentityOrganization.findByOrganization";
  public static final String QUERY_BY_IDENTITY = "IdentityOrganization.findByIdentity";
  public static final String QUERY_APPROVED_BY_IDENTITY = "IdentityOrganization.findApprovedByIdentity";

  @ManyToOne
  public Identity identity;

  @ManyToOne
  public Organization organization;

  public Instant requested;

  public Instant approved;

  public Instant accepted;

  @ManyToOne
  public Identity approvedBy;

  /**
   * Page through members of {@code o}, newest first.
   */
  public static Uni<List<IdentityOrganization>> findByOrganization(Organization o, int limit, String cursor) {
    PageCursor pc = PageCursor.decode(cursor);
    return find("#" + QUERY_BY_ORGANIZATION,
      Parameters.with("organizationId", o.id)
        .and("cursorCreated", pc == null ? null : pc.created)
        .and("cursorId", pc == null ? null : pc.id))
      .range(0, PageCursor.clampLimit(limit) - 1)
      .list();
  }

  /**
   * Page through the organizations {@code i} is a member of, newest first.
   */
  public static Uni<List<IdentityOrganization>> findByIdentity(Identity i, int limit, String cursor) {
    PageCursor pc = PageCursor.decode(cursor);
    return find("#" + QUERY_BY_IDENTITY,
      Parameters.with("identityId", i.id)
        .and("cursorCreated", pc == null ? null : pc.created)
        .and("cursorId", pc == null ? null : pc.id))
      .range(0, PageCursor.clampLimit(limit) - 1)
      .list();
  }

  /**
   * Page through membership requests that {@code i} approved, newest first.
   */
  public static Uni<List<IdentityOrganization>> findApprovedByIdentity(Identity i, int limit, String cursor) {
    PageCursor pc = PageCursor.decode(cursor);
    return find("#" + QUERY_APPROVED_BY_IDENTITY,
      Parameters.with("identityId", i.id)
        .and("cursorCreated", pc == null ? null : pc.created)
        .and("cursorId", pc == null ? null : pc.id))
      .range(0, PageCursor.clampLimit(limit) - 1)
      .list();
  }
}
