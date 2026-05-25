package app.dissipate.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
  @SuppressWarnings("deprecation")
  void generatePkcs552tHash_ShouldReturn20ByteArray() {
    byte[] salt = encryptionUtil.generateSalt16Byte();
    byte[] hash = encryptionUtil.generatePkcs552tHash("value", salt);
    assertNotNull(hash, "Generated hash should not be null");
    assertEquals(20, hash.length, "Generated hash should be 20 bytes long");
  }

  @Test
  void encryptDecrypt_ShouldRoundTrip() {
    String key = "test-key-only-for-unit-tests";
    String plaintext = "sensitive value";
    byte[] cipher = encryptionUtil.encrypt(plaintext, key);
    assertNotNull(cipher, "Encrypted output should not be null");
    String roundTripped = encryptionUtil.decrypt(cipher, key);
    assertEquals(plaintext, roundTripped, "decrypt(encrypt(x)) should equal x");
  }

  @Test
  void hashPassword_returnsPhcFormatString() {
    String hash = encryptionUtil.hashPassword("correct horse battery staple");
    assertNotNull(hash);
    assertTrue(hash.startsWith("$argon2id$v=19$m="
        + EncryptionUtil.ARGON2_MEMORY_KIB
        + ",t=" + EncryptionUtil.ARGON2_ITERATIONS
        + ",p=" + EncryptionUtil.ARGON2_PARALLELISM + "$"),
      "expected PHC prefix with current params; got: " + hash);
    // Two hashes of the same password should differ thanks to per-hash salt.
    assertNotEquals(hash, encryptionUtil.hashPassword("correct horse battery staple"));
  }

  @Test
  void hashPassword_verifyPassword_roundTrip() {
    String plaintext = "correct horse battery staple";
    String hash = encryptionUtil.hashPassword(plaintext);

    EncryptionUtil.VerifyResult ok = encryptionUtil.verifyPassword(plaintext, hash);
    assertTrue(ok.matched(), "matching password should verify");
    assertFalse(ok.needsRehash(), "current params should not flag rehash");

    EncryptionUtil.VerifyResult wrong = encryptionUtil.verifyPassword("nope", hash);
    assertFalse(wrong.matched(), "wrong password must not verify");
    assertFalse(wrong.needsRehash());
  }

  @Test
  @SuppressWarnings("deprecation")
  void verifyPassword_legacyPbkdf2_matchesAndFlagsRehash() {
    byte[] salt = encryptionUtil.generateSalt16Byte();
    byte[] legacy = encryptionUtil.generatePkcs552tHash("hunter2", salt);

    EncryptionUtil.VerifyResult ok = encryptionUtil.verifyLegacyPbkdf2("hunter2", legacy, salt);
    assertTrue(ok.matched(), "legacy PBKDF2 round-trip should match");
    assertTrue(ok.needsRehash(), "legacy match must request rehash");

    EncryptionUtil.VerifyResult wrong = encryptionUtil.verifyLegacyPbkdf2("hunter3", legacy, salt);
    assertFalse(wrong.matched());
    assertFalse(wrong.needsRehash());
  }

  @Test
  void verifyPassword_argon2idWithWeakerParams_flagsRehash() {
    // Hand-craft a PHC string with deliberately weaker (but valid) parameters
    // so we can confirm needsRehash fires on a successful match.
    String plaintext = "password123";
    byte[] salt = encryptionUtil.generateSalt16Byte();
    // Use the legacy generateArgon2Sensitive (m=1048576,t=4,p=1) — different
    // from the current target — and stitch a PHC string from it.
    byte[] hash = encryptionUtil.generateArgon2Sensitive(plaintext, salt);
    java.util.Base64.Encoder b64 = java.util.Base64.getEncoder().withoutPadding();
    String phc = "$argon2id$v=19$m=1048576,t=4,p=1$"
      + b64.encodeToString(salt) + "$" + b64.encodeToString(hash);

    EncryptionUtil.VerifyResult ok = encryptionUtil.verifyPassword(plaintext, phc);
    assertTrue(ok.matched(), "weaker-params Argon2id should still verify");
    assertTrue(ok.needsRehash(), "params different from target must request rehash");
  }
}
