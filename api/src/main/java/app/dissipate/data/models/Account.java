package app.dissipate.data.models;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.utils.EncryptionUtil;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import app.dissipate.exceptions.DissipateRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Entity
@Table(name = "accounts")
public class Account extends DefaultPanacheEntityWithTimestamps {

  public static final String ID_GENERATOR_KEY = "Account";

  public int region;

  public AccountStatus status;

  public byte[] passwordHash;

  public byte[] passwordSalt;

  public String timezone;

  public Locale locale;

  @Transient
  public String password;

  @OneToMany(
    mappedBy = "account",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  public List<AccountPhone> phones = new ArrayList<>();

  @OneToMany(
    mappedBy = "account",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  public List<AccountEmail> emails = new ArrayList<>();

  @OneToMany(
    mappedBy = "account",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  public List<Identity> identities = new ArrayList<>();

  @Override
  public <T extends PanacheEntityBase> Uni<T> persist() {
    throw new DissipateRuntimeException("don't use, use persistAndFlush(EncryptionUtil encryptionUtil) instead");
  }

  @Override
  public <T extends PanacheEntityBase> Uni<T> persistAndFlush() {
    throw new DissipateRuntimeException("don't use, use persistAndFlush(EncryptionUtil encryptionUtil) instead");
  }

  public Uni<Account> persistAndFlush(EncryptionUtil encryptionUtil) {
    hashFields(encryptionUtil);
    return super.persistAndFlush();
  }

  @WithSpan
  public void hashFields(EncryptionUtil eu) {
    if (this.password != null) {
      byte[] saltBytes;
      if (this.passwordSalt == null) {
        saltBytes = eu.generateSalt16Byte();
      } else {
        saltBytes = this.passwordSalt;
      }

      this.passwordHash = eu.generatePkcs552tHash(this.password, saltBytes);
      this.password = null;
      this.passwordSalt = saltBytes;
    }
  }

  public static Uni<Account> findBySrcId(String srcId) {
    return find("srcId", srcId).firstResult();
  }

  public static Uni<Account> createNewAnonymousAccount(Locale locale,
                                                       SnowflakeIdGenerator snowflakeIdGenerator,
                                                       EncryptionUtil encryptionUtil) {
    Account account = new Account();
    account.id = snowflakeIdGenerator.generate(Account.ID_GENERATOR_KEY);
    account.status = AccountStatus.PENDING;
    account.locale = locale;
    return account.persistAndFlush(encryptionUtil);
  }

  public static Uni<Account> createNewAnonymousAccount(Locale locale,
                                                       String email,
                                                       SnowflakeIdGenerator snowflakeIdGenerator,
                                                       EncryptionUtil encryptionUtil) {
    Account account = new Account();
    account.id = snowflakeIdGenerator.generate(Account.ID_GENERATOR_KEY);
    account.status = AccountStatus.PENDING;
    account.locale = locale;
    return account.persistAndFlush(encryptionUtil).onItem().transformToUni(a -> {
      AccountEmail accountEmail = new AccountEmail();
      accountEmail.id = snowflakeIdGenerator.generate(AccountEmail.ID_GENERATOR_KEY);
      accountEmail.account = a;
      accountEmail.email = email;
      account.emails.add(accountEmail);
      return accountEmail.persistAndFlush().onItem().transform(ae -> a);
    });
  }


}

