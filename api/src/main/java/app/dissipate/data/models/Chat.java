package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "chats")
public class Chat extends DefaultPanacheEntityWithTimestamps {

  @OneToMany(mappedBy = "chat")
  public List<ChatParticipant> participants;

  @OneToMany(mappedBy = "chat")
  public List<ChatEvent> events;

}
