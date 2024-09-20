package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "content_review_comments")
public class ContentReviewComment extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne
  public Post post;

  @ManyToOne
  public Identity identity;

  public String comment;

}
