package app.dissipate.data.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "post_reactions")
public class PostReaction extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne(optional = false)
  public Post post;

  @ManyToOne(optional = false)
  public Identity identity;

  @Column(nullable = false, length = 4)
  public String emoji;
}
