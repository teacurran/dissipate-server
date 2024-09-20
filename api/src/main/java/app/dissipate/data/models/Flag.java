package app.dissipate.data.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Locale;

@Entity
@Table(name = "flags")
public class Flag extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  public Identity reportedBy;

  public FlagContentType type;

  @ManyToOne
  public Post post;

  @ManyToOne
  public ChatEvent chatEvent;

  @ManyToOne
  public Identity identity;

  public Locale commentLocale;

  @Column(columnDefinition = "TEXT")
  public String comment;

}
