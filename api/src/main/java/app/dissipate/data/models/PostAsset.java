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
@Table(name = "post_assets")
@NamedQuery(name = PostAsset.QUERY_BY_POST, query = """
    SELECT pa
    FROM PostAsset pa
    WHERE pa.post.id = :postId
      AND (:cursorCreated IS NULL OR pa.created < :cursorCreated
        OR (pa.created = :cursorCreated AND pa.id < :cursorId))
    ORDER BY pa.created DESC, pa.id DESC
  """)
public class PostAsset extends DefaultPanacheEntityWithTimestamps {

  public static final String QUERY_BY_POST = "PostAsset.findByPost";

  @ManyToOne
  public Post post;

  @ManyToOne
  public Asset asset;

  /**
   * Page through assets attached to {@code p}, newest first.
   */
  public static Uni<List<PostAsset>> findByPost(Post p, int limit, String cursor) {
    PageCursor pc = PageCursor.decode(cursor);
    return find("#" + QUERY_BY_POST,
      Parameters.with("postId", p.id)
        .and("cursorCreated", pc == null ? null : pc.created)
        .and("cursorId", pc == null ? null : pc.id))
      .range(0, PageCursor.clampLimit(limit) - 1)
      .list();
  }
}
