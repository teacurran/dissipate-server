package app.dissipate.data.models;

import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "account_emails", indexes = {
  @Index(name = "ix_account_emails_email_validated", columnList = "email,validated"),
})
@NamedQuery(name = AccountEmail.QUERY_FIND_BY_EMAIL_VALIDATED,
  query = """
FROM AccountEmail
WHERE email = :email
AND validated != null
AND deleted = false
""")
public class AccountEmail extends DefaultPanacheEntityWithTimestamps {

  public static final String QUERY_FIND_BY_EMAIL_VALIDATED = "AccountEmail.findByEmailValidated";

  @ManyToOne
  public Account account;

  public String email;

  public Instant validated;

  public boolean isPrimary;

  public void setEmail(String email) {
    this.email = email == null ? null : email.toLowerCase();
  }

  public static Uni<AccountEmail> findByEmailValidated(String email) {
    return find(AccountEmail.QUERY_FIND_BY_EMAIL_VALIDATED, "email", email).firstResult();
  }
}
