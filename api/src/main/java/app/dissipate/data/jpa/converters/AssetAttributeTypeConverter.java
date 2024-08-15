package app.dissipate.data.jpa.converters;

import app.dissipate.data.models.AssetAttributeType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AssetAttributeTypeConverter implements AttributeConverter<AssetAttributeType, String> {

  @Override
  public String convertToDatabaseColumn(AssetAttributeType type) {
    if (type == null) {
      return null;
    }
    return type.name();
  }

  @Override
  public AssetAttributeType convertToEntityAttribute(String dbType) {
    return AssetAttributeType.valueOf(dbType);
  }
}
