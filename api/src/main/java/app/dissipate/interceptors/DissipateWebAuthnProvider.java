package app.dissipate.interceptors;

import io.quarkus.security.webauthn.WebAuthnUserProvider;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.auth.webauthn.Authenticator;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Set;

@ApplicationScoped
public class DissipateWebAuthnProvider implements WebAuthnUserProvider {
  @Override
  public Uni<List<Authenticator>> findWebAuthnCredentialsByUserName(String userName) {
    return null;
  }

  @Override
  public Uni<List<Authenticator>> findWebAuthnCredentialsByCredID(String credentialId) {
    return null;
  }

  @Override
  public Uni<Void> updateOrStoreWebAuthnCredentials(Authenticator authenticator) {
    return null;
  }

  @Override
  public Set<String> getRoles(String userName) {
    return WebAuthnUserProvider.super.getRoles(userName);
  }
}
