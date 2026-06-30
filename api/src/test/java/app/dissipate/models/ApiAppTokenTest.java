package app.dissipate.models;

import app.dissipate.data.models.ApiAppToken;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiAppTokenTest {

  @Test
  void isExpiredReflectsTheExpiryInstant() {
    ApiAppToken token = new ApiAppToken();
    Instant now = Instant.now();

    token.expiresAt = now.minusSeconds(1);
    assertTrue(token.isExpired(now));

    token.expiresAt = now.plusSeconds(60);
    assertFalse(token.isExpired(now));

    token.expiresAt = null; // a token with no expiry never expires
    assertFalse(token.isExpired(now));
  }
}
