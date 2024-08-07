package app.dissipate.data.models;

import io.smallrye.mutiny.Uni;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
public class Account extends DefaultPanacheEntityWithTimestamps {
  public AccountStatus status;

  @OneToMany(
    mappedBy = "account",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  public List<AccountPhone> phones = new ArrayList<AccountPhone>();

  @OneToMany(
    mappedBy = "account",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  public List<AccountEmail> emails = new ArrayList<AccountEmail>();

  @OneToMany(
    mappedBy = "account",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  public List<Identity> identities = new ArrayList<Identity>();

  @ManyToOne(fetch = FetchType.EAGER)
  public Organization organization;

  public static Uni<Account> findBySrcId(String srcId) {
    return find("srcId", srcId).firstResult();
  }

  @Override
  public Uni<Account> persist() {
    return super.persist();
  }

  @Override
  public Uni<Account> persistAndFlush() {
    return super.persistAndFlush();
  }

  public enum AccountStatus {
    PENDING(1),
    ACTIVE(2),
    DISABLED(3),
    SUSPENDED(4),
    BANNED(5);

    private final int value;

    private AccountStatus(int value) {
      this.value = value;
    }

    public static AccountStatus fromValue(int id) {
      for (AccountStatus item : AccountStatus.values()) {
        if (item.getValue() == id) {
          return item;
        }
      }
      return null;
    }

    public int getValue() {
      return value;
    }

  }
}

