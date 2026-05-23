package app.dissipate.data.models;

import app.dissipate.utils.PageCursor;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "organization_hierarchies")
@NamedQuery(name = OrganizationHierarchy.QUERY_BY_ORGANIZATION, query = """
    SELECT oh
    FROM OrganizationHierarchy oh
    WHERE oh.organization.id = :organizationId
      AND (:cursorCreated IS NULL OR oh.created < :cursorCreated
        OR (oh.created = :cursorCreated AND oh.id < :cursorId))
    ORDER BY oh.created DESC, oh.id DESC
  """)
public class OrganizationHierarchy extends DefaultPanacheEntityWithTimestamps {

  public static final String QUERY_BY_ORGANIZATION = "OrganizationHierarchy.findByOrganization";

  @ManyToOne
  public Organization organization;

  @ManyToOne
  public Hierarchy hierarchy;

  /**
   * Page through hierarchies attached to {@code o}, newest first.
   */
  public static Uni<List<OrganizationHierarchy>> findByOrganization(Organization o, int limit, String cursor) {
    PageCursor pc = PageCursor.decode(cursor);
    return find("#" + QUERY_BY_ORGANIZATION,
      Parameters.with("organizationId", o.id)
        .and("cursorCreated", pc == null ? null : pc.created)
        .and("cursorId", pc == null ? null : pc.id))
      .range(0, PageCursor.clampLimit(limit) - 1)
      .list();
  }
}
