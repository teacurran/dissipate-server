package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "membership_receipts")
public class MembershipReceipt extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne
  Account account;

}
