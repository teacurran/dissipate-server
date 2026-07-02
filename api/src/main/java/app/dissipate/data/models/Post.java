package app.dissipate.data.models;

import jakarta.persistence.*;

import java.util.Map;

/**
 * * posts tied to an organization will always also have an identity
 * * organization settings will determine if the identity is displayed along-side the post
 * * a permission will be available for other members of the organization to see who posted or not
 *
 * <p>NOTE: associated child rows (assets, reactions, views, content reviews)
 * are intentionally NOT mapped as {@code @OneToMany} collections on this
 * entity. Each can grow unbounded for a viral post and traversing them through
 * Hibernate Reactive would OOM or throw {@code LazyInitializationException}.
 * Use the paged static finders instead:
 * <ul>
 *   <li>{@link PostAsset#findByPost(Post, int, String)}</li>
 *   <li>{@link PostReaction#findByPost(Post, int, String)}</li>
 *   <li>{@link ContentReview#findByPost(Post, int, String)}</li>
 * </ul>
 */
@Entity
@Table(name = "posts")
public class Post extends FederatedEntity {

  @ManyToOne(optional = false)
  public Identity identity;

  @ManyToOne
  public Organization organization;

  @ManyToOne
  public IdentityAvatar identityAvatar;

  @ManyToOne
  public Post replyTo;

  @Column(nullable = false, columnDefinition = "BOOLEAN NOT NULL DEFAULT FALSE")
  public boolean deleted;

  @Column(length = 4)
  String defaultReactionEmoji;

  @Column(columnDefinition = "TEXT")
  public String caption;

  @Column(columnDefinition = "TEXT")
  String body;

  @Override
  public String entityType() {
    return "Post";
  }

  /** The editable content that a revision captures (structural relations are not part of the edit history). */
  @Override
  public Map<String, Object> auditSnapshot() {
    return Map.of(
        "caption", caption == null ? "" : caption,
        "body", body == null ? "" : body,
        "deleted", deleted,
        "defaultReactionEmoji", defaultReactionEmoji == null ? "" : defaultReactionEmoji);
  }

}
