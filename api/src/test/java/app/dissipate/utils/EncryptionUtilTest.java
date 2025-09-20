package app.dissipate.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EncryptionUtilTest {

  private final EncryptionUtil encryptionUtil = new EncryptionUtil();

  @Test
  void generateSalt16Byte_ShouldReturn16ByteArray() {
    byte[] salt = encryptionUtil.generateSalt16Byte();
    assertNotNull(salt, "Generated salt should not be null");
    assertEquals(16, salt.length, "Generated salt should be 16 bytes long");
  }

  @Test
  void generateArgon2Sensitive_ShouldReturn32ByteArray() {
    byte[] salt = encryptionUtil.generateSalt16Byte();
    byte[] sensitive = encryptionUtil.generateArgon2Sensitive("password", salt);
    assertNotNull(sensitive, "Generated sensitive should not be null");
    assertEquals(32, sensitive.length, "Generated sensitive should be 32 bytes long");
  }

  @Test
  void generatePkcs552tHash_ShouldReturn20ByteArray() {
    byte[] salt = encryptionUtil.generateSalt16Byte();
    byte[] hash = encryptionUtil.generatePkcs552tHash("value", salt);
    assertNotNull(hash, "Generated hash should not be null");
    assertEquals(20, hash.length, "Generated hash should be 20 bytes long");
  }
}
