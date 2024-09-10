package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_participants")
public class ChatParticipant extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  public Chat chat;

  @ManyToOne
  public Identity identity;

  public ChatParticipantType type;
}
