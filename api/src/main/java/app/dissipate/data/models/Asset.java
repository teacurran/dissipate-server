package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "assets")
public class Asset extends DefaultPanacheEntityWithTimestamps {

  public UUID tft;

  @ManyToOne
  public Identity creator;

  @ManyToOne
  public Identity owner;

  public AssetType type;


}
