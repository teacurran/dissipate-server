package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "assets", indexes = {
  @Index(columnList = "tft", unique = true),
  @Index(columnList = "hash")
})
public class Asset extends DefaultPanacheEntityWithTimestamps {

  public UUID tft;

  String hash;

  @ManyToOne
  public Identity creator;

  @ManyToOne
  public Identity owner;

  public AssetType type;

  @OneToMany(mappedBy = "asset")
  public List<AssetAttribute> attributes;

}
