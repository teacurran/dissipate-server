package app.dissipate.data.models;

import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "account_emails", indexes = {
  @Index(name = "ix_account_emails_email_validated", columnList = "email,validated"),
})
@NamedQueries({
  @NamedQuery(name = AccountEmail.QUERY_FIND_BY_EMAIL_VALIDATED,
    query = """
      FROM AccountEmail
      WHERE email = :email
      AND validated IS NOT NULL
      """),
  @NamedQuery(name = AccountEmail.QUERY_FIND_BY_VALIDATED_EMAIL,
    query = """
      FROM AccountEmail
      WHERE LOWER(email) = LOWER(:email)
      AND validated IS NOT NULL
      """),
  @NamedQuery(name = AccountEmail.QUERY_FIND_BY_VALIDATED_EMAIL_WITH_ACCOUNT,
    query = """
      FROM AccountEmail ae
      JOIN FETCH ae.account
      WHERE LOWER(ae.email) = LOWER(:email)
      AND ae.validated IS NOT NULL
      """)
})
public class AccountEmail extends DefaultPanacheEntityWithTimestamps {

  public static final String ID_GENERATOR_KEY = "AccountEmail";

  public static final String QUERY_FIND_BY_EMAIL_VALIDATED = "AccountEmail.findByEmailValidated";
  public static final String QUERY_FIND_BY_VALIDATED_EMAIL = "AccountEmail.findByValidatedEmail";
  public static final String QUERY_FIND_BY_VALIDATED_EMAIL_WITH_ACCOUNT = "AccountEmail.findByValidatedEmailWithAccount";

  @ManyToOne
  public Account account;

  public String email;

  public Instant validated;

  public boolean isPrimary;

  public void setEmail(String email) {
    this.email = email == null ? null : email.toLowerCase();
  }

  /**
   * @deprecated Case-sensitive match against the raw {@code email} column. Prefer
   * {@link #findByValidatedEmail(String)} which performs a case-insensitive lookup
   * and aligns with the partial unique index {@code uidx_account_emails_validated_lower}.
   */
  @Deprecated
  public static Uni<AccountEmail> findByEmailValidated(String email) {
    return find("#" + AccountEmail.QUERY_FIND_BY_EMAIL_VALIDATED, Parameters.with("email", email)).firstResult();
  }

  /**
   * Case-insensitive lookup of the (at most one) validated owner of the given email.
   * Backed by the partial unique index {@code uidx_account_emails_validated_lower},
   * so it returns the single account that has completed OTP validation for this
   * address, or {@code null} if none.
   */
  public static Uni<AccountEmail> findByValidatedEmail(String email) {
    return find("#" + AccountEmail.QUERY_FIND_BY_VALIDATED_EMAIL, Parameters.with("email", email)).firstResult();
  }

  /**
   * Like {@link #findByValidatedEmail(String)} but eagerly fetches the owning {@link Account} so the
   * caller can read account state (password hash, lockout) within its own session — used by login.
   */
  public static Uni<AccountEmail> findByValidatedEmailWithAccount(String email) {
    return find("#" + AccountEmail.QUERY_FIND_BY_VALIDATED_EMAIL_WITH_ACCOUNT,
      Parameters.with("email", email)).firstResult();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Uni<AccountEmail> persist() {
    return super.persist();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Uni<AccountEmail> persistAndFlush() {
    return super.persistAndFlush();
  }

  public Uni<AccountEmail> markValidated() {
    validated = Instant.now();
    return persistAndFlush();
  }

}
