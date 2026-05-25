package app.dissipate.data.models;

import app.dissipate.utils.PageCursor;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "chat_events", indexes = {
  @Index(columnList = "chat_id,created")
})
@NamedQuery(name = ChatEvent.QUERY_BY_CHAT, query = """
    SELECT e
    FROM ChatEvent e
    WHERE e.chat.id = :chatId
      AND (:cursorCreated IS NULL OR e.created < :cursorCreated
        OR (e.created = :cursorCreated AND e.id < :cursorId))
    ORDER BY e.created DESC, e.id DESC
  """)
public class ChatEvent extends DefaultPanacheEntityWithTimestamps {

  public static final String QUERY_BY_CHAT = "ChatEvent.findByChat";

  @ManyToOne
  public Chat chat;

  @ManyToOne
  public Identity identity;

  public ChatEventType type;

  @Column(columnDefinition = "TEXT")
  public String message;

  @OneToMany(mappedBy = "event",
    cascade = CascadeType.ALL,
    orphanRemoval = true)
  public List<ChatEventAsset> assets;

  /**
   * Page through chat events for {@code c}, newest first.
   *
   * @param c      chat to look up events for
   * @param limit  page size (clamped to {@link PageCursor#MAX_PAGE_SIZE})
   * @param cursor opaque cursor previously returned, or {@code null} for the first page
   */
  public static Uni<List<ChatEvent>> findByChat(Chat c, int limit, String cursor) {
    PageCursor pc = PageCursor.decode(cursor);
    return find("#" + QUERY_BY_CHAT,
      Parameters.with("chatId", c.id)
        .and("cursorCreated", pc == null ? null : pc.created)
        .and("cursorId", pc == null ? null : pc.id))
      .range(0, PageCursor.clampLimit(limit) - 1)
      .list();
  }
}
