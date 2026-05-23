package app.dissipate.data.jpa.converters;

import app.dissipate.utils.EncryptionUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link EncryptedStringConverter}. Exercises the converter outside CDI by
 * directly priming the static fields on {@link PiiEncryptionConfig} via reflection.
 */
class EncryptedStringConverterTest {

  private static final String TEST_KEY = "unit-test-key-with-enough-entropy-for-tests-only";

  @BeforeAll
  static void primeStaticConfig() throws Exception {
    Field keyField = PiiEncryptionConfig.class.getDeclaredField("KEY");
    keyField.setAccessible(true);
    keyField.set(null, TEST_KEY);

    Field euField = PiiEncryptionConfig.class.getDeclaredField("EU");
    euField.setAccessible(true);
    euField.set(null, new EncryptionUtil());
  }

  @Test
  void roundTripsAStringValue() {
    EncryptedStringConverter c = new EncryptedStringConverter();
    String plaintext = "Jane Q. Public";

    byte[] persisted = c.convertToDatabaseColumn(plaintext);
    assertNotNull(persisted, "ciphertext should not be null");
    assertTrue(persisted.length > 0, "ciphertext should not be empty");

    String decoded = c.convertToEntityAttribute(persisted);
    assertEquals(plaintext, decoded, "round-trip should recover the original plaintext");
  }

  @Test
  void persistedBytesDoNotContainPlaintext() {
    EncryptedStringConverter c = new EncryptedStringConverter();
    String plaintext = "742 Evergreen Terrace";

    byte[] persisted = c.convertToDatabaseColumn(plaintext);

    String asUtf8 = new String(persisted, StandardCharsets.UTF_8);
    assertFalse(asUtf8.contains(plaintext),
      "stored bytes must not contain the plaintext substring");

    // Also confirm no contiguous plaintext bytes appear anywhere in the array.
    byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
    assertFalse(containsSubsequence(persisted, plaintextBytes),
      "stored bytes must not contain plaintext byte sequence");
  }

  @Test
  void nullPassesThroughBothDirections() {
    EncryptedStringConverter c = new EncryptedStringConverter();
    assertNull(c.convertToDatabaseColumn(null));
    assertNull(c.convertToEntityAttribute(null));
  }

  @Test
  void distinctCiphertextsForSameInput() {
    EncryptedStringConverter c = new EncryptedStringConverter();
    byte[] a = c.convertToDatabaseColumn("repeated-value");
    byte[] b = c.convertToDatabaseColumn("repeated-value");
    // AES-GCM with a fresh IV per call should not produce identical bytes.
    assertFalse(java.util.Arrays.equals(a, b),
      "two encryptions of the same plaintext must differ (random IV)");
  }

  private static boolean containsSubsequence(byte[] haystack, byte[] needle) {
    if (needle.length == 0 || needle.length > haystack.length) {
      return false;
    }
    outer:
    for (int i = 0; i <= haystack.length - needle.length; i++) {
      for (int j = 0; j < needle.length; j++) {
        if (haystack[i + j] != needle[j]) {
          continue outer;
        }
      }
      return true;
    }
    return false;
  }
}
