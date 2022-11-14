package app.dissipate.data.jpa.converters;

import app.dissipate.data.models.Account.AccountStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class AccountStatusConverter implements AttributeConverter<AccountStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(AccountStatus type) {
        if (type == null) {
            return 0;
        }
        return type.getValue();
    }

    @Override
    public AccountStatus convertToEntityAttribute(Integer dbType) {
        return AccountStatus.fromValue(dbType);
    }
}
