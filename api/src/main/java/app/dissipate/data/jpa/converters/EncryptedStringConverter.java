package app.dissipate.data.jpa.converters;

import app.dissipate.utils.EncryptionUtil;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter that transparently encrypts String fields on write and decrypts on read
 * using {@link EncryptionUtil} (AES-GCM). The encryption key is sourced via
 * {@link PiiEncryptionConfig} from the {@code dissipate.pii.key} config property.
 *
 * <p>Apply with {@code @Convert(converter = EncryptedStringConverter.class)} on a {@code String}
 * field that is persisted as {@code BYTEA}. Do NOT mark this {@code autoApply = true} — we only
 * want explicit opt-in on PII columns.
 */
@RegisterForReflection
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, byte[]> {

  @Override
  public byte[] convertToDatabaseColumn(String attribute) {
    if (attribute == null) {
      return null;
    }
    EncryptionUtil eu = PiiEncryptionConfig.encryptionUtil();
    return eu.encrypt(attribute, PiiEncryptionConfig.key());
  }

  @Override
  public String convertToEntityAttribute(byte[] dbData) {
    if (dbData == null) {
      return null;
    }
    EncryptionUtil eu = PiiEncryptionConfig.encryptionUtil();
    return eu.decrypt(dbData, PiiEncryptionConfig.key());
  }
}
