package app.dissipate.auth;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.Map;

/**
 * Per-minute rate-limit ceilings, expressed in method-{@code cost} units (an expensive method
 * consumes more of the budget than a cheap one). Resolved per principal: first-party users get
 * {@link #userPerMinute()}; third-party apps get their tier's ceiling, or {@link #appDefaultPerMinute()}
 * when the tier has none configured.
 */
@ConfigMapping(prefix = "dissipate.ratelimit")
public interface RateLimitConfig {

  /** Per-minute cost ceiling for first-party (user) callers. */
  @WithDefault("1200")
  long userPerMinute();

  /** Fallback per-minute cost ceiling for an app whose rate tier has no explicit limit. */
  @WithDefault("600")
  long appDefaultPerMinute();

  /** Per-minute cost ceiling per app rate tier (key = tier name). */
  Map<String, Long> tier();

  /** The ceiling that applies to the given principal. */
  default long limitFor(Principal principal) {
    if (principal.isApp()) {
      Long tierLimit = principal.rateTier() == null ? null : tier().get(principal.rateTier());
      return tierLimit != null ? tierLimit : appDefaultPerMinute();
    }
    return userPerMinute();
  }
}
