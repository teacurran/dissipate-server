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

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
public class Account extends DefaultPanacheEntityWithTimestamps {

  public static final String ID_GENERATOR_KEY = "Account";

  public AccountStatus status;

  public String passwordEncrypted;
  public String passwordSalt;

  @Transient
  public String password;

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

  @Override
  public <T extends PanacheEntityBase> Uni<T> persist() {
    throw new RuntimeException("don't use, use persistAndFlush(EncryptionUtil encryptionUtil) instead");
  }

  @Override
  public <T extends PanacheEntityBase> Uni<T> persistAndFlush() {
    throw new RuntimeException("don't use, use persistAndFlush(EncryptionUtil encryptionUtil) instead");
  }

  public <T extends PanacheEntityBase> Uni<T> persistAndFlush(EncryptionUtil encryptionUtil) {
    hashFields(encryptionUtil);
    return super.persistAndFlush();
  }

  @WithSpan
  public void hashFields(EncryptionUtil eu) {
    if (this.password != null) {
      byte[] saltBytes = null;
      if (this.passwordSalt == null) {
        saltBytes = eu.generateSalt16Byte();
      } else {
        saltBytes = eu.base64Decode(this.passwordSalt);
      }

      this.passwordEncrypted = eu.base64Encode(eu.generatePkcs552tHash(this.password, saltBytes));
      this.password = null;
      this.passwordSalt = eu.base64Encode(saltBytes);
    }
  }

  public static Uni<Account> findBySrcId(String srcId) {
    return find("srcId", srcId).firstResult();
  }

  public static Uni<Account> createNewAnonymousAccount(SnowflakeIdGenerator snowflakeIdGenerator, EncryptionUtil encryptionUtil) {
    Account account = new Account();
    account.id = snowflakeIdGenerator.generate(Account.ID_GENERATOR_KEY);
    account.status = AccountStatus.PENDING;
    return account.persistAndFlush(encryptionUtil);
  }
}

