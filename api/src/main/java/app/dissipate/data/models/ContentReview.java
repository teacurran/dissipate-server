package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "content_reviews")
public class ContentReview extends DefaultPanacheEntityWithTimestamps {

  public FlagContentType type;

  @ManyToOne
  public Post post;

  @ManyToOne
  public ChatEvent chatEvent;

  @ManyToOne
  public Identity identity;

  public ContentReviewResult result;

  public boolean bot;
}
