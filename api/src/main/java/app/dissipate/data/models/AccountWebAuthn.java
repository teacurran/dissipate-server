package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import io.vertx.ext.auth.webauthn.Authenticator;
import io.vertx.ext.auth.webauthn.PublicKeyCredential;
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
  public PublicKeyCredential alg;

  /**
   * The list of X509 certificates encoded as base64url.
   */
  @OneToMany(mappedBy = "webAuthnCredential")
  public List<WebAuthnCertificate> x5c = new ArrayList<>();

  public String fmt;

  public AccountWebAuthn() {
    // Default constructor
  }

  public AccountWebAuthn(Authenticator authenticator, Account account) {
    aaguid = authenticator.getAaguid();
    if(authenticator.getAttestationCertificates() != null)
      alg = authenticator.getAttestationCertificates().getAlg();
    counter = authenticator.getCounter();
    credentialId = authenticator.getCredID();
    fmt = authenticator.getFmt();
    publicKey = authenticator.getPublicKey();

    // type is always public-key
    //type = authenticator.getType();

    // we have to look up the account by 'username' here
    //    account = Account.findBySrcId();
    //    userName = authenticator.getUserName();

    if(authenticator.getAttestationCertificates() != null
      && authenticator.getAttestationCertificates().getX5c() != null) {
      for (String x5c : authenticator.getAttestationCertificates().getX5c()) {
        WebAuthnCertificate cert = new WebAuthnCertificate();
        cert.x5c = x5c;
        cert.accountWebAuthn = this;
        this.x5c.add(cert);
      }
    }
    this.account = account;


    // account.webAuthnCredential = this;
  }
}
