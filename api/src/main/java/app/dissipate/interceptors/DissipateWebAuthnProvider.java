package app.dissipate.interceptors;

import io.quarkus.security.webauthn.WebAuthnUserProvider;
import io.quarkus.security.webauthn.WebAuthnCredentialRecord;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Set;

@ApplicationScoped
public class DissipateWebAuthnProvider implements WebAuthnUserProvider {

  @Override
  public Uni<List<WebAuthnCredentialRecord>> findByUsername(String username) {
    return null;
  }

  @Override
  public Uni<WebAuthnCredentialRecord> findByCredentialId(String credentialId) {
    return Uni.createFrom().nullItem();
  }

  @Override
  public Uni<Void> update(String credentialId, long counter) {
    return WebAuthnUserProvider.super.update(credentialId, counter);
  }

  @Override
  public Uni<Void> store(WebAuthnCredentialRecord credentialRecord) {
    return WebAuthnUserProvider.super.store(credentialRecord);
  }

  @Override
  public Set<String> getRoles(String userName) {
    return WebAuthnUserProvider.super.getRoles(userName);
  }

}
