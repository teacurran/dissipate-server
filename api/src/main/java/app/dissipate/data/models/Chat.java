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

  @OneToMany(mappedBy = "chat")
  public List<ChatParticipant> participants;

  @OneToMany(mappedBy = "chat")
  public List<ChatEvent> events;


}
