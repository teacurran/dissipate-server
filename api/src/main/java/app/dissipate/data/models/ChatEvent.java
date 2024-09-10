package app.dissipate.data.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "chat_events")
public class ChatEvent extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  public Chat chat;

  @ManyToOne
  public Identity identity;

  public ChatEventType type;

  public String message;

  @OneToMany(mappedBy = "event",
    cascade = CascadeType.ALL,
    orphanRemoval = true)
  public List<ChatEventAsset> assets;
}
