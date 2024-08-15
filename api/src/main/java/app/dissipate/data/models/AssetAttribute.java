package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "asset_attributes")
public class AssetAttribute extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne(optional = false)
  public Asset asset;

  public AssetAttributeType assetAttributeType;

  @ManyToOne
  public AssetCollectionAttribute assetCollectionAttribute;

  public Double numericValue;

  public String textValue;

  public Double rank;

}
