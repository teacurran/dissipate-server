package app.dissipate.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit coverage for the high-entropy token primitives used by the OAuth client-credentials issuer:
 * opaque-token generation, SHA-256 hashing, and the constant-time match.
 */
class EncryptionUtilTokenTest {

  private final EncryptionUtil enc = new EncryptionUtil();

  @Test
  void sha256IsDeterministicAndDistinct() {
    assertEquals(enc.sha256("abc"), enc.sha256("abc"));
    assertNotEquals(enc.sha256("abc"), enc.sha256("abd"));
  }

  @Test
  void opaqueTokensAreUniqueAndUrlSafe() {
    String a = enc.generateOpaqueToken();
    String b = enc.generateOpaqueToken();
    assertNotEquals(a, b);
    assertFalse(a.isBlank());
    assertTrue(a.matches("[A-Za-z0-9_-]+"), "expected URL-safe base64 without padding: " + a);
  }

  @Test
  void matchesSha256CoversHitMissAndNulls() {
    String token = enc.generateOpaqueToken();
    String hash = enc.sha256(token);
    assertTrue(enc.matchesSha256(token, hash));
    assertFalse(enc.matchesSha256("wrong-token", hash));
    assertFalse(enc.matchesSha256(null, hash));
    assertFalse(enc.matchesSha256(token, null));
  }
}
