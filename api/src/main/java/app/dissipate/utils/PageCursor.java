package app.dissipate.utils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Objects;

/**
 * Opaque page cursor used to paginate large JPA collections that used to be
 * mapped as unbounded {@code @OneToMany} on entities such as
 * {@code Identity}, {@code Chat}, {@code Post} and {@code Organization}.
 *
 * <p>Wire format (after base64-url decoding):
 * <pre>{@literal {createdInstant}|{id}}</pre>
 * where {@code createdInstant} is an ISO-8601 instant string (e.g.
 * {@code 2026-05-23T12:34:56.789Z}) and {@code id} is the row's primary key
 * string. Tuple is used to break {@code created} ties deterministically.
 *
 * <p>The format is intentionally simple — do not over-design. The cursor is
 * opaque to clients; bumping the encoding is a server-only concern.
 */
public final class PageCursor {

  public static final int DEFAULT_PAGE_SIZE = 50;
  public static final int MAX_PAGE_SIZE = 200;

  public final Instant created;
  public final String id;

  public PageCursor(Instant created, String id) {
    this.created = Objects.requireNonNull(created, "created");
    this.id = Objects.requireNonNull(id, "id");
  }

  /**
   * Encode an instant/id pair into an opaque base64-url cursor string.
   */
  public static String encode(Instant created, String id) {
    String raw = created.toString() + "|" + id;
    return Base64.getUrlEncoder().withoutPadding()
      .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Decode a previously-issued cursor. Returns {@code null} if {@code cursor}
   * is null or blank (meaning "start from the beginning").
   *
   * @throws IllegalArgumentException if the cursor is malformed
   */
  public static PageCursor decode(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return null;
    }
    byte[] decoded;
    try {
      decoded = Base64.getUrlDecoder().decode(cursor);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid cursor: not base64-url", e);
    }
    String raw = new String(decoded, StandardCharsets.UTF_8);
    int sep = raw.indexOf('|');
    if (sep <= 0 || sep == raw.length() - 1) {
      throw new IllegalArgumentException("Invalid cursor: missing separator");
    }
    String createdStr = raw.substring(0, sep);
    String id = raw.substring(sep + 1);
    Instant created;
    try {
      created = Instant.parse(createdStr);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid cursor: bad instant", e);
    }
    return new PageCursor(created, id);
  }

  /**
   * Clamp a requested page size into the {@code [1, MAX_PAGE_SIZE]} range,
   * defaulting to {@link #DEFAULT_PAGE_SIZE} when non-positive.
   */
  public static int clampLimit(int requested) {
    if (requested <= 0) {
      return DEFAULT_PAGE_SIZE;
    }
    return Math.min(requested, MAX_PAGE_SIZE);
  }
}
