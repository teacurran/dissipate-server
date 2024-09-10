package app.dissipate.data.jpa.converters;

import app.dissipate.data.models.IdentityBlockType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@RegisterForReflection
@Converter(autoApply = true)
public class IdentityBlockTypeConverter implements AttributeConverter<IdentityBlockType, String> {
  @Override
  public String convertToDatabaseColumn(IdentityBlockType type) {
    if (type == null) {
      return IdentityBlockType.BLOCKED.name();
    }
    return type.name();
  }

  @Override
  public IdentityBlockType convertToEntityAttribute(String dbType) {
    return IdentityBlockType.valueOf(dbType);
  }
}
