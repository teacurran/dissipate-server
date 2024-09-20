package app.dissipate.data.jpa.converters;

import app.dissipate.data.models.ContentReviewResult;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@RegisterForReflection
@Converter(autoApply = true)
public class ContentReviewResultConverter implements AttributeConverter<ContentReviewResult, String> {

  @Override
  public String convertToDatabaseColumn(ContentReviewResult type) {
    if (type == null) {
      return null;
    }
    return type.name();
  }

  @Override
  public ContentReviewResult convertToEntityAttribute(String dbType) {
    return ContentReviewResult.valueOf(dbType);
  }
}
