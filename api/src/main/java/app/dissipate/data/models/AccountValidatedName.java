package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "account_validated_names")
public class AccountValidatedName extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  public Account account;

  public String name;
}
