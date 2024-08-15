package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "asset_histories")
public class AssetHistory extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  Asset asset;

  @ManyToOne
  Identity actor;

  @ManyToOne
  Identity actor2;

  String action;
}
