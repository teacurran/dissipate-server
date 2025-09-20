package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
// WebAuthn imports removed - need to refactor for Quarkus 3.26
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account_web_authns")
public class AccountWebAuthn extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  public Account account;

  public String credentialId;

  public String publicKey;

  public long counter;

  public String aaguid;

  /**
   * The Authenticator attestation certificates object, a JSON like:
   * <pre>{@code
   *   {
   *     "alg": "string",
   *     "x5c": [
   *       "base64"
   *     ]
   *   }
   * }</pre>
   */
  /**
   * The algorithm used for the public credential
   */
  public String alg; // Changed from PublicKeyCredential to String for Quarkus 3.26

  /**
   * The list of X509 certificates encoded as base64url.
   */
  @OneToMany(mappedBy = "accountWebAuthn")
  public List<WebAuthnCertificate> x5c = new ArrayList<>();

  public String fmt;

  public AccountWebAuthn() {
    // Default constructor
  }

  // TODO: Refactor this constructor for Quarkus 3.26 WebAuthn API
  // The Authenticator class from io.vertx.ext.auth.webauthn is no longer available
  // This constructor needs to be updated based on the new WebAuthn implementation
  /*
  public AccountWebAuthn(Authenticator authenticator, Account account) {
    // Previous implementation commented out - needs refactoring
  }
  */
}
