package app.dissipate.data.models;

import jakarta.persistence.Entity;

@Entity
public class MembershipReceipt extends DefaultPanacheEntityWithTimestamps {
  Account account;

}
