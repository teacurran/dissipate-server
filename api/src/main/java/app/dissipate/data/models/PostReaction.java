package app.dissipate.data.models;

import app.dissipate.utils.PageCursor;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "post_reactions")
@NamedQuery(name = PostReaction.QUERY_BY_POST, query = """
    SELECT r
    FROM PostReaction r
    WHERE r.post.id = :postId
      AND (:cursorCreated IS NULL OR r.created < :cursorCreated
        OR (r.created = :cursorCreated AND r.id < :cursorId))
    ORDER BY r.created DESC, r.id DESC
  """)
public class PostReaction extends DefaultPanacheEntityWithTimestamps {

  public static final String QUERY_BY_POST = "PostReaction.findByPost";

  @ManyToOne(optional = false)
  public Post post;

  @ManyToOne(optional = false)
  public Identity identity;

  @Column(nullable = false, length = 4)
  public String emoji;

  /**
   * Page through reactions to {@code p}, newest first.
   */
  public static Uni<List<PostReaction>> findByPost(Post p, int limit, String cursor) {
    PageCursor pc = PageCursor.decode(cursor);
    return find("#" + QUERY_BY_POST,
      Parameters.with("postId", p.id)
        .and("cursorCreated", pc == null ? null : pc.created)
        .and("cursorId", pc == null ? null : pc.id))
      .range(0, PageCursor.clampLimit(limit) - 1)
      .list();
  }
}
