package app.dissipate.data.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * A deployment region. Data locality is a property of the account
 * ({@code Account.home_region}), not of an id — unlike the retired Snowflake scheme, an
 * account can be re-homed by changing a column. {@code dissipate.region} config names the
 * region a given node runs in.
 */
@RegisterForReflection
public enum Region {
  US_EAST("us-east"),
  US_WEST("us-west"),
  EU("eu");

  private final String code;

  Region(String code) {
    this.code = code;
  }

  /** Stable, human-readable wire/storage code (e.g. {@code us-east}). */
  public String code() {
    return code;
  }

  public static Region fromCode(String code) {
    for (Region r : values()) {
      if (r.code.equals(code)) {
        return r;
      }
    }
    throw new IllegalArgumentException("Unknown region code: " + code);
  }
}
