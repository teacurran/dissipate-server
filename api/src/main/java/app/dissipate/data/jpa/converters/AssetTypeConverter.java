package app.dissipate.data.jpa.converters;

import app.dissipate.data.models.AssetType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AssetTypeConverter implements AttributeConverter<AssetType, String> {

  @Override
  public String convertToDatabaseColumn(AssetType type) {
    if (type == null) {
      return null;
    }
    return type.name();
  }

  @Override
  public AssetType convertToEntityAttribute(String dbType) {
    return AssetType.fromValue(dbType);
  }
}
