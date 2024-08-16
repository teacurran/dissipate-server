package app.dissipate.data.jpa.converters;

import app.dissipate.data.models.DelayedJobQueue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DelayedJobQueueConverter implements AttributeConverter<DelayedJobQueue, String> {

  @Override
  public String convertToDatabaseColumn(DelayedJobQueue type) {
    if (type == null) {
      return null;
    }
    return type.name();
  }

  @Override
  public DelayedJobQueue convertToEntityAttribute(String dbType) {
    return DelayedJobQueue.valueOf(dbType);
  }
}
