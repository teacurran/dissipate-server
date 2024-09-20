package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Locale;

@Entity
@Table(name = "flags")
public class Flag extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  public Identity identity;

  @ManyToOne
  public Post post;

  @ManyToOne
  public ChatEvent chatEvent;

  public Locale commentLocale;

  public String comment;

}
