package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class MembershipReceipt extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne
  Account account;

}
