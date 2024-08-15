package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "asset_collection_attributes")
public class AssetCollectionAttribute extends DefaultPanacheEntityWithTimestamps {
  public String name;
  public String description;
  public AssetAttributeType assetAttributeType;

  public Double minValue;

  public Double maxValue;

  public String unit;

  public Double rank;
}
