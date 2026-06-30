package app.dissipate.auth;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The fixed catalog of resource:action scopes a third-party app may be granted. Apps request a
 * subset; {@code DeveloperService} validates requested scopes against this catalog, and the authz
 * pipeline checks a token's held scopes against each method's required scopes.
 */
public final class ScopeCatalog {

  /** All recognised scopes. Grow this as new resource domains ship. */
  public static final Set<String> ALL = Set.of(
      "posts:read",
      "posts:write",
      "follows:read",
      "follows:write",
      "chat:read",
      "identity:read");

  private ScopeCatalog() {
    // catalog of constants
  }

  public static boolean contains(String scope) {
    return ALL.contains(scope);
  }

  /** The requested scopes that are not in the catalog; empty means every requested scope is valid. */
  public static Set<String> unknown(Collection<String> requested) {
    return requested.stream().filter(s -> !ALL.contains(s)).collect(Collectors.toUnmodifiableSet());
  }
}
