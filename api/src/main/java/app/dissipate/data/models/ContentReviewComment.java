package app.dissipate.data.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "content_review_comments")
public class ContentReviewComment extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne
  public Post post;

  @ManyToOne
  public ChatEvent chatEvent;

  @ManyToOne
  public Identity identity;

  @Column(columnDefinition = "TEXT")
  public String comment;

}
