package app.dissipate.data.jpa.converters;

import app.dissipate.data.models.IdentityActionType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@RegisterForReflection
@Converter(autoApply = true)
public class IdentityActionTypeConverter implements AttributeConverter<IdentityActionType, String> {
  @Override
  public String convertToDatabaseColumn(IdentityActionType type) {
    if (type == null) {
      throw new IllegalArgumentException("IdentityActionType cannot be null");
    }
    return type.name();
  }

  @Override
  public IdentityActionType convertToEntityAttribute(String dbType) {
    return IdentityActionType.valueOf(dbType);
  }
}


