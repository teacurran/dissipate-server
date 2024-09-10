package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_event_assets")
public class ChatEventAsset extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne
  public ChatEvent event;

  @ManyToOne
  public Asset asset;

}
