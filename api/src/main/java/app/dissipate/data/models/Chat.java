package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "chats")
@NamedQuery(name = Chat.QUERY_BY_PARTICIPANT, query = """
    SELECT c
    FROM Chat c
    JOIN c.participants p
    WHERE p.identity.id = :userId
""")
public class Chat extends DefaultPanacheEntityWithTimestamps {
  public static final String QUERY_BY_PARTICIPANT = "Chat.findByParticipant";

  /**
   * WARNING: do NOT traverse this collection from application code. It is
   * unbounded (a large group chat may have thousands of participants) and
   * eagerly fetching it under the reactive Hibernate session will OOM or
   * throw {@code LazyInitializationException}. It is kept here ONLY so that
   * the JPQL named query {@link #QUERY_BY_PARTICIPANT} can navigate the
   * relationship server-side. For application access use the paged finders
   * on {@link ChatParticipant} instead.
   */
  @OneToMany(mappedBy = "chat")
  protected List<ChatParticipant> participants;

  // NOTE: the events relationship is intentionally NOT mapped here. Chats
  // accumulate events for the life of the conversation; traversing them
  // through the entity would OOM. Use {@link ChatEvent#findByChat(Chat, int, String)}
  // instead.
}
