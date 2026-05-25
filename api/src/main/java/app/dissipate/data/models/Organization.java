package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * NOTE: associated child rows (organization members, sub-organizations,
 * hierarchies) are intentionally NOT mapped as {@code @OneToMany}
 * collections here. Each can grow into the thousands for a large
 * organization, and traversing them through Hibernate Reactive would OOM or
 * throw {@code LazyInitializationException}. Use the paged static finders
 * instead:
 * <ul>
 *   <li>{@link IdentityOrganization#findByOrganization(Organization, int, String)}</li>
 *   <li>{@link Organization#findChildrenOf(Organization, int, String)}</li>
 *   <li>{@link OrganizationHierarchy#findByOrganization(Organization, int, String)}</li>
 * </ul>
 */
@Entity
@Table(name = "organizations")
public class Organization extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne
  Organization parent;

  @ManyToOne
  HierarchyLevel level;

  public String name;

  /**
   * Page through direct children of {@code parent}, newest first.
   */
  public static io.smallrye.mutiny.Uni<java.util.List<Organization>> findChildrenOf(
      Organization parent, int limit, String cursor) {
    app.dissipate.utils.PageCursor pc = app.dissipate.utils.PageCursor.decode(cursor);
    return find(
        "parent.id = :parentId"
          + " and (:cursorCreated is null or created < :cursorCreated"
          + " or (created = :cursorCreated and id < :cursorId))"
          + " order by created desc, id desc",
        io.quarkus.panache.common.Parameters.with("parentId", parent.id)
          .and("cursorCreated", pc == null ? null : pc.created)
          .and("cursorId", pc == null ? null : pc.id))
      .range(0, app.dissipate.utils.PageCursor.clampLimit(limit) - 1)
      .list();
  }
}
