package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "post_views")
public class PostView extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne(optional = false)
  public Post post;

  @ManyToOne(optional = false)
  public Identity identity;

  @ManyToOne(optional = false)
  public Session session;
}
