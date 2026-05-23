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
@Table(name = "identity_follows")
@NamedQuery(name = IdentityFollow.QUERY_FOLLOWERS_OF, query = """
    SELECT f
    FROM IdentityFollow f
    WHERE f.identity2.id = :identityId
      AND (:cursorCreated IS NULL OR f.created < :cursorCreated
        OR (f.created = :cursorCreated AND f.id < :cursorId))
    ORDER BY f.created DESC, f.id DESC
  """)
@NamedQuery(name = IdentityFollow.QUERY_FOLLOWING_OF, query = """
    SELECT f
    FROM IdentityFollow f
    WHERE f.identity.id = :identityId
      AND (:cursorCreated IS NULL OR f.created < :cursorCreated
        OR (f.created = :cursorCreated AND f.id < :cursorId))
    ORDER BY f.created DESC, f.id DESC
  """)
public class IdentityFollow extends DefaultPanacheEntityWithTimestamps {

  public static final String QUERY_FOLLOWERS_OF = "IdentityFollow.findFollowersOf";
  public static final String QUERY_FOLLOWING_OF = "IdentityFollow.findFollowingOf";

  @ManyToOne(optional = false)
  public Identity identity;

  @ManyToOne
  public Identity identity2;

  public Instant requested;

  public Instant approved;

  public boolean isMutual;

  /**
   * Page through the followers of {@code i} — that is, the rows where
   * {@code identity2 = i}. Newest first.
   *
   * @param i      identity to look up followers for
   * @param limit  page size (clamped to {@link PageCursor#MAX_PAGE_SIZE})
   * @param cursor opaque cursor previously returned, or {@code null} for the first page
   */
  public static Uni<List<IdentityFollow>> findFollowersOf(Identity i, int limit, String cursor) {
    PageCursor c = PageCursor.decode(cursor);
    return find("#" + QUERY_FOLLOWERS_OF,
      Parameters.with("identityId", i.id)
        .and("cursorCreated", c == null ? null : c.created)
        .and("cursorId", c == null ? null : c.id))
      .range(0, PageCursor.clampLimit(limit) - 1)
      .list();
  }

  /**
   * Page through the identities followed by {@code i} — that is, the rows
   * where {@code identity = i}. Newest first.
   */
  public static Uni<List<IdentityFollow>> findFollowingOf(Identity i, int limit, String cursor) {
    PageCursor c = PageCursor.decode(cursor);
    return find("#" + QUERY_FOLLOWING_OF,
      Parameters.with("identityId", i.id)
        .and("cursorCreated", c == null ? null : c.created)
        .and("cursorId", c == null ? null : c.id))
      .range(0, PageCursor.clampLimit(limit) - 1)
      .list();
  }
}
