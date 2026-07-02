package app.dissipate.auth;

import app.dissipate.data.models.AccountRole;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Unit coverage for {@link RateLimitConfig#limitFor} tier resolution. */
class RateLimitConfigTest {

  private static RateLimitConfig config(long user, long appDefault, Map<String, Long> tiers) {
    return new RateLimitConfig() {
      @Override public long userPerMinute() { return user; }
      @Override public long appDefaultPerMinute() { return appDefault; }
      @Override public Map<String, Long> tier() { return tiers; }
    };
  }

  private static Principal app(String rateTier) {
    return new Principal(null, null, null, Set.of(),
        UUID.fromString("00000000-0000-0000-0000-000000000007"), rateTier);
  }

  @Test
  void usersGetTheUserCeiling() {
    Principal user = new Principal(UUID.fromString("00000000-0000-0000-0000-000000000001"),
        null, AccountRole.USER, Set.of(), null, null);
    assertEquals(1200, config(1200, 600, Map.of()).limitFor(user));
  }

  @Test
  void appsGetTheirTierCeilingWhenConfigured() {
    assertEquals(6000, config(1200, 600, Map.of("premium", 6000L)).limitFor(app("premium")));
  }

  @Test
  void appsFallBackToTheDefaultForUnknownOrNullTier() {
    RateLimitConfig config = config(1200, 600, Map.of("premium", 6000L));
    assertEquals(600, config.limitFor(app("default")));
    assertEquals(600, config.limitFor(app(null)));
  }
}
