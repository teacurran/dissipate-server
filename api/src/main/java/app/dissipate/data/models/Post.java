package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

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

  public String caption;

  @ManyToOne
  public Post replyTo;

  @OneToMany(mappedBy = "post")
  List<PostAsset> postAssets = new ArrayList<>();

  String defaultReaction;
}
