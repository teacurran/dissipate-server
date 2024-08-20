package app.dissipate.data.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter(autoApply = true)
public class LocaleConverter implements AttributeConverter<Locale, String> {

  @Override
  public String convertToDatabaseColumn(Locale type) {
    if (type == null) {
      return null;
    }

    return type.toLanguageTag();

  }

  @Override
  public Locale convertToEntityAttribute(String dbType) {
    return LocaleConverter.fromValue(dbType);
  }

  public static Locale fromValue(String value) {
    if (value == null) {
      return Locale.getDefault();
    }

    return Locale.forLanguageTag(value);
  }
}
