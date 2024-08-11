package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import java.time.ZonedDateTime;

@Entity
public class AccountMembership extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne
  Account account;

  ZonedDateTime begins;

  ZonedDateTime ends;

  AccountMembershipStatus status;

}
