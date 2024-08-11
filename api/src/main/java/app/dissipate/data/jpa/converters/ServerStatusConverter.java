package app.dissipate.data.jpa.converters;

import app.dissipate.data.models.ServerStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ServerStatusConverter implements AttributeConverter<ServerStatus, String> {

  @Override
  public String convertToDatabaseColumn(ServerStatus type) {
    if (type == null) {
      return ServerStatus.UNKNOWN.name();
    }
    return type.name();
  }

  @Override
  public ServerStatus convertToEntityAttribute(String dbType) {
    return ServerStatus.fromValue(dbType);
  }
}
