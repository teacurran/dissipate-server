package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "asset_collections")
public class AssetCollection extends DefaultPanacheEntityWithTimestamps {
}
