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

    return type.toString();

  }

  @Override
  public Locale convertToEntityAttribute(String dbType) {
    return LocaleConverter.fromValue(dbType);
  }

  public static Locale fromValue(String value) {
    if (value == null) {
      return Locale.getDefault();
    }

    String[] parts = value.split("_");
    Locale.Builder builder = new Locale.Builder().setLanguage(parts[0]);

    if (parts.length > 1) {
      builder.setRegion(parts[1]);
    }

    if (parts.length > 2) {
      builder.setVariant(parts[2]);
    }

    return builder.build();
  }
}
