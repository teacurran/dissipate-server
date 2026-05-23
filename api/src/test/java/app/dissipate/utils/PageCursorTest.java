package app.dissipate.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageCursorTest {

  @Test
  void encodeDecode_roundTripsInstantAndId() {
    Instant created = Instant.parse("2026-05-23T12:34:56.789Z");
    String id = "01HXYZABC1234567";

    String cursor = PageCursor.encode(created, id);
    PageCursor decoded = PageCursor.decode(cursor);

    assertEquals(created, decoded.created, "round-tripped instant should match");
    assertEquals(id, decoded.id, "round-tripped id should match");
  }

  @Test
  void decode_returnsNullForBlankCursor() {
    assertNull(PageCursor.decode(null));
    assertNull(PageCursor.decode(""));
    assertNull(PageCursor.decode("   "));
  }

  @Test
  void decode_rejectsGarbageInput() {
    assertThrows(IllegalArgumentException.class,
      () -> PageCursor.decode("!!! not base64 !!!"));
  }

  @Test
  void decode_rejectsMissingSeparator() {
    // base64-url for "no-separator-here"
    String bad = java.util.Base64.getUrlEncoder().withoutPadding()
      .encodeToString("no-separator-here".getBytes());
    assertThrows(IllegalArgumentException.class, () -> PageCursor.decode(bad));
  }

  @Test
  void decode_rejectsBadInstant() {
    String bad = java.util.Base64.getUrlEncoder().withoutPadding()
      .encodeToString("not-an-instant|some-id".getBytes());
    assertThrows(IllegalArgumentException.class, () -> PageCursor.decode(bad));
  }

  @Test
  void encode_handlesIdsContainingPipeCharactersInSuffix() {
    // The id portion after the first '|' is taken verbatim; ensure round-trip
    // is intact when the id itself happens to contain extra pipe characters.
    Instant created = Instant.parse("2026-01-01T00:00:00Z");
    String id = "weird|id|with|pipes";

    PageCursor decoded = PageCursor.decode(PageCursor.encode(created, id));

    assertEquals(created, decoded.created);
    assertEquals(id, decoded.id);
  }

  @Test
  void clampLimit_appliesDefaultsAndCeiling() {
    assertEquals(PageCursor.DEFAULT_PAGE_SIZE, PageCursor.clampLimit(0));
    assertEquals(PageCursor.DEFAULT_PAGE_SIZE, PageCursor.clampLimit(-5));
    assertEquals(25, PageCursor.clampLimit(25));
    assertEquals(PageCursor.MAX_PAGE_SIZE, PageCursor.clampLimit(10_000));
  }

  @Test
  void encode_isUrlSafeBase64() {
    String cursor = PageCursor.encode(Instant.now(), "abc");
    // URL-safe base64 has no '+' or '/'
    assertTrue(cursor.indexOf('+') < 0, "no + characters");
    assertTrue(cursor.indexOf('/') < 0, "no / characters");
  }
}
