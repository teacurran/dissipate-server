package app.dissipate.data.jpa.converters;

import app.dissipate.data.models.AccountMembershipStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AccountMembershipStatusConverter implements AttributeConverter<AccountMembershipStatus, String> {

  @Override
  public String convertToDatabaseColumn(AccountMembershipStatus type) {
    if (type == null) {
      return null;
    }
    return type.name();
  }

  @Override
  public AccountMembershipStatus convertToEntityAttribute(String dbType) {
    return AccountMembershipStatus.fromValue(dbType);
  }
}
