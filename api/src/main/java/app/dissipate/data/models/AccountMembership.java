package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.ZonedDateTime;

/**
 * Represents a paid membership to an account.
 * */
@Entity
@Table(name = "account_memberships")
public class AccountMembership extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne
  Account account;

  ZonedDateTime begins;

  ZonedDateTime ends;

  AccountMembershipStatus status;

}
