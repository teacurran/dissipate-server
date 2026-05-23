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
@Table(name = "content_reviews")
@NamedQuery(name = ContentReview.QUERY_BY_POST, query = """
    SELECT cr
    FROM ContentReview cr
    WHERE cr.post.id = :postId
      AND (:cursorCreated IS NULL OR cr.created < :cursorCreated
        OR (cr.created = :cursorCreated AND cr.id < :cursorId))
    ORDER BY cr.created DESC, cr.id DESC
  """)
public class ContentReview extends DefaultPanacheEntityWithTimestamps {

  public static final String QUERY_BY_POST = "ContentReview.findByPost";

  public FlagContentType type;

  @ManyToOne
  public Post post;

  @ManyToOne
  public ChatEvent chatEvent;

  @ManyToOne
  public Identity identity;

  public ContentReviewResult result;

  public boolean bot;

  /**
   * Page through content reviews for {@code p}, newest first.
   */
  public static Uni<List<ContentReview>> findByPost(Post p, int limit, String cursor) {
    PageCursor pc = PageCursor.decode(cursor);
    return find("#" + QUERY_BY_POST,
      Parameters.with("postId", p.id)
        .and("cursorCreated", pc == null ? null : pc.created)
        .and("cursorId", pc == null ? null : pc.id))
      .range(0, PageCursor.clampLimit(limit) - 1)
      .list();
  }
}
