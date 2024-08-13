package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "post_assets")
public class PostAsset extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne
  public Post post;

  @ManyToOne
  public Asset asset;

}
