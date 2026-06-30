package app.dissipate.auth;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScopeCatalogTest {

  @Test
  void recognisesCatalogScopes() {
    assertTrue(ScopeCatalog.contains("posts:read"));
    assertTrue(ScopeCatalog.contains("identity:read"));
    assertFalse(ScopeCatalog.contains("posts:delete"));
  }

  @Test
  void unknownReturnsOnlyTheUnrecognisedScopes() {
    assertEquals(Set.of(), ScopeCatalog.unknown(List.of("posts:read", "follows:write")));
    assertEquals(Set.of("nope:read"), ScopeCatalog.unknown(List.of("posts:read", "nope:read")));
    assertEquals(Set.of(), ScopeCatalog.unknown(List.of()));
  }
}
