package app.dissipate.utils;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.crypto.params.KeyParameter;

@ApplicationScoped
public class EncryptionUtil {

  @WithSpan
  public byte[] generateSalt16Byte() {
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16];
    random.nextBytes(salt);
    return salt;
  }

  @WithSpan
  public byte[] generateArgon2Sensitive(String password, byte[] salt) {
    int opsLimit = 4;
    int memLimit = 1048576;
    int outputLength = 32;
    int parallelism = 1;
    Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
      .withVersion(Argon2Parameters.ARGON2_VERSION_13) // 19
      .withIterations(opsLimit)
      .withMemoryAsKB(memLimit)
      .withParallelism(parallelism)
      .withSalt(salt);
    Argon2BytesGenerator gen = new Argon2BytesGenerator();
    gen.init(builder.build());
    byte[] result = new byte[outputLength];
    gen.generateBytes(password.getBytes(StandardCharsets.UTF_8), result, 0, result.length);
    return result;
  }

  @WithSpan
  public byte[] generatePkcs552tHash(String value, byte[] salt) {
    int hashBytes = 20;

    PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator();
    generator.init(value.getBytes(StandardCharsets.UTF_8), salt, 1024);

    return ((KeyParameter)generator.generateDerivedParameters(8*hashBytes)).getKey();
  }

  // AES-GCM parameters. Output layout: [12-byte IV || ciphertext || 16-byte auth tag].
  private static final String AES_GCM = "AES/GCM/NoPadding";
  private static final int GCM_IV_LENGTH = 12;
  private static final int GCM_TAG_LENGTH_BITS = 128;

  @WithSpan
  public byte[] encrypt(String input, String key) {
    if (input == null) {
      return null;
    }
    if (key == null) {
      throw new IllegalArgumentException("encryption key must not be null");
    }
    try {
      byte[] iv = new byte[GCM_IV_LENGTH];
      new SecureRandom().nextBytes(iv);
      Cipher cipher = Cipher.getInstance(AES_GCM);
      cipher.init(Cipher.ENCRYPT_MODE, deriveAesKey(key), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
      byte[] cipherText = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
      byte[] out = new byte[iv.length + cipherText.length];
      System.arraycopy(iv, 0, out, 0, iv.length);
      System.arraycopy(cipherText, 0, out, iv.length, cipherText.length);
      return out;
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("AES-GCM encrypt failed", e);
    }
  }

  @WithSpan
  public String decrypt(byte[] input, String key) {
    if (input == null) {
      return null;
    }
    if (key == null) {
      throw new IllegalArgumentException("encryption key must not be null");
    }
    if (input.length < GCM_IV_LENGTH + (GCM_TAG_LENGTH_BITS / 8)) {
      throw new IllegalArgumentException("encrypted payload too short");
    }
    try {
      byte[] iv = new byte[GCM_IV_LENGTH];
      System.arraycopy(input, 0, iv, 0, GCM_IV_LENGTH);
      byte[] cipherText = new byte[input.length - GCM_IV_LENGTH];
      System.arraycopy(input, GCM_IV_LENGTH, cipherText, 0, cipherText.length);
      Cipher cipher = Cipher.getInstance(AES_GCM);
      cipher.init(Cipher.DECRYPT_MODE, deriveAesKey(key), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
      return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("AES-GCM decrypt failed", e);
    }
  }

  // Derive a 256-bit AES key from the provided string using SHA-256. The configured key value
  // should be high-entropy (e.g. base64-encoded 32 random bytes) so the hash adds no security
  // assumption; this just normalizes arbitrary input lengths into a valid AES-256 key.
  private SecretKeySpec deriveAesKey(String key) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
      return new SecretKeySpec(digest, "AES");
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  @WithSpan
  public String base64Encode(byte[] input) {
    return Base64.getEncoder().encodeToString(input);
  }

  @WithSpan
  public byte[] base64Decode(String input) {
    return Base64.getDecoder().decode(input);
  }
}
