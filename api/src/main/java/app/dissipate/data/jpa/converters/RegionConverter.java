package app.dissipate.data.jpa.converters;

import app.dissipate.data.models.Region;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Persists a {@link Region} as its stable wire code (e.g. {@code us-east}) rather than its enum name. */
@Converter(autoApply = false)
public class RegionConverter implements AttributeConverter<Region, String> {

  @Override
  public String convertToDatabaseColumn(Region region) {
    return region == null ? null : region.code();
  }

  @Override
  public Region convertToEntityAttribute(String code) {
    return code == null ? null : Region.fromCode(code);
  }
}
