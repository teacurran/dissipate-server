package app.dissipate.data.models;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.jpa.converters.EncryptedStringConverter;
import app.dissipate.utils.EncryptionUtil;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
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

  /**
   * First-party role used by the gRPC auth pipeline to enforce {@code MethodPolicy.min_role}.
   * Stored as its name (VARCHAR); defaults to {@link AccountRole#USER} for every account.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public AccountRole role = AccountRole.USER;

  /**
   * Legacy PBKDF2 password hash. New accounts use {@link #passwordHashStr}
   * (Argon2id, PHC-format). Retained so existing accounts can still log in
   * during the transition; rehashed to Argon2id on the next successful login.
   */
  public byte[] passwordHash;

  /**
   * Legacy salt for {@link #passwordHash}. Not used by Argon2id hashes — the
   * salt is embedded in {@link #passwordHashStr}.
   */
  public byte[] passwordSalt;

  /**
   * Self-describing Argon2id PHC-format password hash, e.g.
   * {@code $argon2id$v=19$m=19456,t=2,p=1$<salt-b64>$<hash-b64>}.
   * Populated for all newly-set passwords. When non-null, takes precedence
   * over {@link #passwordHash}.
   */
  public String passwordHashStr;

  public String timezone;

  public Locale locale;

  // PII columns are encrypted at rest via EncryptedStringConverter (AES-GCM).
  // Java type remains String; persisted as BYTEA. Column names are *_enc to force a
  // migration that drops any pre-existing plaintext rather than silently double-encoding.
  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "first_name_enc", columnDefinition = "BYTEA")
  public String firstName;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "last_name_enc", columnDefinition = "BYTEA")
  public String lastName;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "address1_enc", columnDefinition = "BYTEA")
  public String address1;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "address2_enc", columnDefinition = "BYTEA")
  public String address2;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "city_enc", columnDefinition = "BYTEA")
  public String city;

  @ManyToOne
  public State state;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "postal_code_enc", columnDefinition = "BYTEA")
  public String postalCode;

  @ManyToOne
  public Country country;


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
      // New passwords are hashed with Argon2id and stored in PHC format.
      // The legacy PBKDF2 fields are cleared so verification routes through
      // the Argon2id path going forward.
      this.passwordHashStr = eu.hashPassword(this.password);
      this.passwordHash = null;
      this.passwordSalt = null;
      this.password = null;
    }
  }

  /**
   * Verify a plaintext password against this account's stored hash.
   *
   * Routes to Argon2id when {@link #passwordHashStr} is populated, otherwise
   * falls back to the legacy PBKDF2 hash. On a successful match the result's
   * {@code needsRehash} flag indicates the caller should call
   * {@link #rehashPassword(EncryptionUtil, String)} and persist.
   */
  @WithSpan
  public EncryptionUtil.VerifyResult verifyPassword(EncryptionUtil eu, String plaintext) {
    if (this.passwordHashStr != null) {
      return eu.verifyPassword(plaintext, this.passwordHashStr);
    }
    if (this.passwordHash != null && this.passwordSalt != null) {
      return eu.verifyLegacyPbkdf2(plaintext, this.passwordHash, this.passwordSalt);
    }
    return new EncryptionUtil.VerifyResult(false, false);
  }

  /**
   * Re-hash {@code plaintext} with current Argon2id parameters and clear the
   * legacy PBKDF2 fields. Caller is responsible for persisting.
   */
  @WithSpan
  public void rehashPassword(EncryptionUtil eu, String plaintext) {
    this.passwordHashStr = eu.hashPassword(plaintext);
    this.passwordHash = null;
    this.passwordSalt = null;
  }

  public static Uni<Account> findBySrcId(String srcId) {
    return find("srcId", srcId).firstResult();
  }

  public static Uni<Account> createNewAnonymousAccount(Locale locale,
                                                       SnowflakeIdGenerator snowflakeIdGenerator,
                                                       EncryptionUtil encryptionUtil) {
    Account account = new Account();
    account.id = snowflakeIdGenerator.generate(Account.ID_GENERATOR_KEY);
    account.status = AccountStatus.ANONYMOUS;
    account.locale = locale;
    return account.persistAndFlush(encryptionUtil);
  }

  public static Uni<Account> createNewAnonymousAccount(Locale locale,
                                                       String email,
                                                       SnowflakeIdGenerator snowflakeIdGenerator,
                                                       EncryptionUtil encryptionUtil) {
    Account account = new Account();
    account.id = snowflakeIdGenerator.generate(Account.ID_GENERATOR_KEY);
    account.status = AccountStatus.ANONYMOUS;
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

