package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "content_reviews")
public class ContentReview extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne
  public Post post;

  @ManyToOne
  public ChatEvent chatEvent;

  public ContentReviewResult result;

  @ManyToOne
  public Identity identity;

  public boolean bot;
}
