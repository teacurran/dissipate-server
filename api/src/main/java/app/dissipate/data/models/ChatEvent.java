package app.dissipate.data.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "chat_events")
public class ChatEvent extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  public Chat chat;

  @ManyToOne
  public Identity identity;

  public ChatEventType type;

  @Lob
  public String message;

  @OneToMany(mappedBy = "event",
    cascade = CascadeType.ALL,
    orphanRemoval = true)
  public List<ChatEventAsset> assets;
}
