package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
public class Post extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne(optional = false)
  public Identity identity;

  public String caption;

  @OneToMany(mappedBy = "post")
  List<PostAsset> postAssets = new ArrayList<>();
}
