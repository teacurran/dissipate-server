package app.dissipate.data.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * * posts tied to an organization will always also have an identity
 * * organization settings will determine if the identity is displayed along-side the post
 * * a permission will be available for other members of the organization to see who posted or not
 */
@Entity
@Table(name = "posts")
public class Post extends DefaultPanacheEntityWithTimestamps {

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

  @OneToMany(mappedBy = "post")
  List<PostAsset> postAssets = new ArrayList<>();

  @OneToMany(mappedBy = "post")
  List<ContentReview> contentReviews = new ArrayList<>();

}
