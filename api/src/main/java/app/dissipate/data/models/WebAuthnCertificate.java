package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "webauthn_certificates")
public class WebAuthnCertificate extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne
  public AccountWebAuthn accountWebAuthn;

  /**
   * The list of X509 certificates encoded as base64url.
   */
  public String x5c;
}
