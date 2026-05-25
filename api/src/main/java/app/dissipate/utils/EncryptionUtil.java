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

  /**
   * Current Argon2id parameters. Based on the OWASP Password Storage Cheat Sheet
   * recommendation: "Use Argon2id with a minimum configuration of 19 MiB of memory,
   * an iteration count of 2, and 1 degree of parallelism."
   *
   * We pick a slightly stronger but still mainstream profile:
   *   m = 19456 KiB (19 MiB), t = 2, p = 1, output = 32 bytes, salt = 16 bytes.
   *
   * See: https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
   */
  public static final int ARGON2_MEMORY_KIB = 19456;
  public static final int ARGON2_ITERATIONS = 2;
  public static final int ARGON2_PARALLELISM = 1;
  public static final int ARGON2_HASH_LENGTH = 32;
  public static final int ARGON2_SALT_LENGTH = 16;

  private static final String PHC_ARGON2ID_PREFIX = "$argon2id$";

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

  /**
   * Legacy PBKDF2 hash. Retained only so we can VERIFY hashes that were
   * persisted before the migration to Argon2id. Do not use for new hashes.
   *
   * @deprecated Use {@link #hashPassword(String)} for new hashing.
   *             Kept for verification of legacy hashes.
   */
  @Deprecated
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


  /**
   * Hash a plaintext password using Argon2id with the current OWASP-recommended
   * parameters. Returns a self-describing PHC-format string of the form:
   *
   *   $argon2id$v=19$m=19456,t=2,p=1$<salt-b64>$<hash-b64>
   *
   * Both salt and hash are encoded with RFC 4648 base64 without padding,
   * matching the canonical PHC encoding used by reference Argon2 tooling.
   */
  @WithSpan
  public String hashPassword(String plaintext) {
    if (plaintext == null) {
      throw new IllegalArgumentException("plaintext must not be null");
    }
    byte[] salt = new byte[ARGON2_SALT_LENGTH];
    new SecureRandom().nextBytes(salt);
    byte[] hash = argon2idRaw(plaintext, salt,
      ARGON2_MEMORY_KIB, ARGON2_ITERATIONS, ARGON2_PARALLELISM, ARGON2_HASH_LENGTH);
    Base64.Encoder b64 = Base64.getEncoder().withoutPadding();
    return PHC_ARGON2ID_PREFIX
      + "v=" + Argon2Parameters.ARGON2_VERSION_13
      + "$m=" + ARGON2_MEMORY_KIB
      + ",t=" + ARGON2_ITERATIONS
      + ",p=" + ARGON2_PARALLELISM
      + "$" + b64.encodeToString(salt)
      + "$" + b64.encodeToString(hash);
  }

  /**
   * Verify a plaintext password against a stored hash. Supports both the new
   * Argon2id PHC-format strings and the legacy PBKDF2 byte hashes (which
   * require the original salt to be supplied via {@link #verifyPassword(String, String, byte[])}).
   */
  @WithSpan
  public VerifyResult verifyPassword(String plaintext, String storedHash) {
    return verifyPassword(plaintext, storedHash, null);
  }

  /**
   * Verify a plaintext password against either:
   *   1. a new Argon2id PHC-format string (legacySalt ignored), or
   *   2. the legacy byte-array PBKDF2 hash, base64-encoded into storedHash
   *      and accompanied by the original salt in legacySalt.
   *
   * The result reports both whether the password matched and whether the stored
   * hash should be re-hashed to the current Argon2id parameters.
   */
  @WithSpan
  public VerifyResult verifyPassword(String plaintext, String storedHash, byte[] legacySalt) {
    if (plaintext == null || storedHash == null) {
      return new VerifyResult(false, false);
    }
    if (storedHash.startsWith(PHC_ARGON2ID_PREFIX)) {
      return verifyArgon2idPhc(plaintext, storedHash);
    }
    // Legacy PBKDF2 path: storedHash is base64 of the legacy 20-byte digest.
    if (legacySalt == null) {
      return new VerifyResult(false, false);
    }
    byte[] expected;
    try {
      expected = Base64.getDecoder().decode(storedHash);
    } catch (IllegalArgumentException e) {
      return new VerifyResult(false, false);
    }
    return verifyLegacyPbkdf2(plaintext, expected, legacySalt);
  }

  /**
   * Verify a plaintext password against the legacy PBKDF2 hash stored as raw
   * bytes (the historical {@code Account.passwordHash} column). Always reports
   * {@code needsRehash = true} on a successful match.
   */
  @WithSpan
  public VerifyResult verifyLegacyPbkdf2(String plaintext, byte[] expectedHash, byte[] salt) {
    if (plaintext == null || expectedHash == null || salt == null) {
      return new VerifyResult(false, false);
    }
    byte[] actual = generatePkcs552tHash(plaintext, salt);
    boolean matched = MessageDigest.isEqual(actual, expectedHash);
    return new VerifyResult(matched, matched);
  }

  private VerifyResult verifyArgon2idPhc(String plaintext, String phc) {
    // Format: $argon2id$v=19$m=<m>,t=<t>,p=<p>$<salt-b64>$<hash-b64>
    String[] parts = phc.split("\\$");
    // parts: ["", "argon2id", "v=19", "m=...,t=...,p=...", "<salt>", "<hash>"]
    if (parts.length != 6 || !"argon2id".equals(parts[1])) {
      return new VerifyResult(false, false);
    }
    int version;
    int memKiB;
    int iterations;
    int parallelism;
    byte[] salt;
    byte[] expected;
    try {
      version = parseKv(parts[2], "v");
      String[] params = parts[3].split(",");
      if (params.length != 3) {
        return new VerifyResult(false, false);
      }
      memKiB = parseKv(params[0], "m");
      iterations = parseKv(params[1], "t");
      parallelism = parseKv(params[2], "p");
      Base64.Decoder b64 = Base64.getDecoder();
      salt = b64.decode(parts[4]);
      expected = b64.decode(parts[5]);
    } catch (IllegalArgumentException e) {
      return new VerifyResult(false, false);
    }
    byte[] actual = argon2idRaw(plaintext, salt, memKiB, iterations, parallelism, expected.length);
    boolean matched = MessageDigest.isEqual(actual, expected);
    boolean needsRehash = matched && (
      version != Argon2Parameters.ARGON2_VERSION_13
        || memKiB != ARGON2_MEMORY_KIB
        || iterations != ARGON2_ITERATIONS
        || parallelism != ARGON2_PARALLELISM
        || expected.length != ARGON2_HASH_LENGTH
    );
    return new VerifyResult(matched, needsRehash);
  }

  private static int parseKv(String segment, String expectedKey) {
    int eq = segment.indexOf('=');
    if (eq < 0 || !expectedKey.equals(segment.substring(0, eq))) {
      throw new IllegalArgumentException("expected key " + expectedKey + " in segment " + segment);
    }
    return Integer.parseInt(segment.substring(eq + 1));
  }

  private static byte[] argon2idRaw(String password, byte[] salt,
                                    int memKiB, int iterations, int parallelism, int outputLen) {
    Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
      .withVersion(Argon2Parameters.ARGON2_VERSION_13)
      .withIterations(iterations)
      .withMemoryAsKB(memKiB)
      .withParallelism(parallelism)
      .withSalt(salt);
    Argon2BytesGenerator gen = new Argon2BytesGenerator();
    gen.init(builder.build());
    byte[] result = new byte[outputLen];
    gen.generateBytes(password.getBytes(StandardCharsets.UTF_8), result, 0, result.length);
    return result;
  }

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

  /**
   * Outcome of a password verification.
   *
   * @param matched      true iff the plaintext matches the stored hash.
   * @param needsRehash  true iff the caller should re-hash with current
   *                     Argon2id parameters and persist (only meaningful
   *                     when {@code matched} is true).
   */
  public record VerifyResult(boolean matched, boolean needsRehash) { }
}
