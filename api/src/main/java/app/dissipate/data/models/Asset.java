package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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

}
