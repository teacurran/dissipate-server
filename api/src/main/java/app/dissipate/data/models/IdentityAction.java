package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "identity_actions")
public class IdentityAction extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne
  public Identity identity;

  IdentityActionType type;

  @ManyToOne
  public Identity targetIdentity;

  @ManyToOne
  public Post post;
}
