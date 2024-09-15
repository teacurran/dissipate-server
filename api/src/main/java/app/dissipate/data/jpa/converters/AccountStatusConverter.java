package app.dissipate.data.jpa.converters;

import app.dissipate.data.models.AccountStatus;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@RegisterForReflection
@Converter(autoApply = true)
public class AccountStatusConverter implements AttributeConverter<AccountStatus, String> {

  @Override
  public String convertToDatabaseColumn(AccountStatus type) {
    if (type == null) {
      return AccountStatus.ANONYMOUS.name();
    }
    return type.name();
  }

  @Override
  public AccountStatus convertToEntityAttribute(String dbType) {
    return AccountStatus.valueOf(dbType);
  }
}
