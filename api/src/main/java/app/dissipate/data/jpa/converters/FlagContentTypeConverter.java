package app.dissipate.data.jpa.converters;

import app.dissipate.data.models.FlagContentType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@RegisterForReflection
@Converter(autoApply = true)
public class FlagContentTypeConverter implements AttributeConverter<FlagContentType, String> {

  @Override
  public String convertToDatabaseColumn(FlagContentType type) {
    if (type == null) {
      return null;
    }
    return type.name();
  }

  @Override
  public FlagContentType convertToEntityAttribute(String dbType) {
    return FlagContentType.valueOf(dbType);
  }
}
