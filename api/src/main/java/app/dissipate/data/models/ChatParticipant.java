package app.dissipate.data.models;

import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "chat_participants")
@NamedQuery(name = ChatParticipant.QUERY_FIND_OTHER_PARTICIPANTS,
  query = """
    SELECT cp
    FROM ChatParticipant cp
    WHERE cp.chat.id = :chatId
    AND cp.identity.id != :excludeIdentityId
    """)
public class ChatParticipant extends DefaultPanacheEntityWithTimestamps {

  public static final String QUERY_FIND_OTHER_PARTICIPANTS = "ChatParticipant.findOtherParticipants";

  @ManyToOne
  public Chat chat;

  @ManyToOne
  public Identity identity;

  public ChatParticipantType type;

  public static Uni<List<ChatParticipant>> findOtherParticipants(String chatId, String excludeIdentityId) {
    return find("#" + QUERY_FIND_OTHER_PARTICIPANTS,
      Parameters.with("chatId", chatId).and("excludeIdentityId", excludeIdentityId)).list();
  }
}
