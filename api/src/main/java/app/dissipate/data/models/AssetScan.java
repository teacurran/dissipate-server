package app.dissipate.data.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "asset_scans")
public class AssetScan extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  public Asset asset;

  @Column(columnDefinition = "TEXT")
  public String results;
}
