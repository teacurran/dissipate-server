package app.dissipate.services.grpc;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.models.Account;
import app.dissipate.data.models.AccountEmail;
import app.dissipate.data.models.AccountRole;
import app.dissipate.data.models.AccountStatus;
import app.dissipate.data.models.ApiApp;
import app.dissipate.data.models.ApiAppStatus;
import app.dissipate.data.models.Session;
import app.dissipate.data.models.SessionValidation;
import app.dissipate.utils.EncryptionUtil;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;

/**
 * Test-only helper that seeds a live, validated session directly (bypassing the email/OTP flow)
 * so {@link GrpcAuthPipelineTest} can exercise the gRPC auth pipeline end-to-end without Mailpit.
 */
@ApplicationScoped
public class GrpcAuthTestSeeder {

  @Inject
  SnowflakeIdGenerator idGenerator;

  @Inject
  EncryptionUtil encryptionUtil;

  /** Create an ACTIVE USER account + validated session; returns the session sid. */
  @WithTransaction
  public Uni<String> seedValidatedSession() {
    return seedSession(AccountRole.USER);
  }

  /** Create an ACTIVE VERIFIED account + validated session; returns the session sid. */
  @WithTransaction
  public Uni<String> seedVerifiedSession() {
    return seedSession(AccountRole.VERIFIED);
  }

  private Uni<String> seedSession(AccountRole role) {
    return Account.createNewAnonymousAccount(Locale.ENGLISH, idGenerator, encryptionUtil)
        .onItem().transformToUni(account -> {
          account.status = AccountStatus.ACTIVE;
          account.role = role;
          Session session = new Session();
          session.account = account;
          return session.persistAndFlush().onItem().transformToUni(saved -> {
            SessionValidation sv = new SessionValidation();
            sv.id = idGenerator.generate(SessionValidation.ID_GENERATOR_KEY);
            sv.session = saved;
            sv.token = "SEEDED";
            sv.validated = Instant.now();
            return sv.persistAndFlush().onItem().transform(ignored -> saved.id.toString());
          });
        });
  }

  /** Create an ACTIVE account with a validated email and the given password (for login tests). */
  @WithTransaction
  public Uni<Void> seedAccountWithPassword(String email, String password) {
    return seedAccount(email, account -> account.password = password);
  }

  /** Create an ACTIVE account with a validated email but no password set. */
  @WithTransaction
  public Uni<Void> seedAccountWithoutPassword(String email) {
    return seedAccount(email, account -> { /* leave all password fields null */ });
  }

  /**
   * Create an ACTIVE account whose password is stored as a legacy PBKDF2 hash (not Argon2id), so a
   * successful login exercises LoginMethod's rehash-on-login branch.
   */
  @WithTransaction
  public Uni<Void> seedAccountWithLegacyPassword(String email, String password) {
    return seedAccount(email, account -> {
      byte[] salt = new byte[16];
      new SecureRandom().nextBytes(salt);
      account.passwordHash = encryptionUtil.generatePkcs552tHash(password, salt);
      account.passwordSalt = salt;
      account.passwordHashStr = null; // force the legacy verification path
    });
  }

  /** Register an API app (with a verified owner account) and the SHA-256 of its client secret. */
  @WithTransaction
  public Uni<Void> seedApiApp(String clientId, String clientSecret, String scopes, ApiAppStatus status) {
    return Account.createNewAnonymousAccount(Locale.ENGLISH, idGenerator, encryptionUtil)
        .onItem().transformToUni(owner -> {
          owner.status = AccountStatus.ACTIVE;
          owner.role = AccountRole.VERIFIED;
          return owner.persistAndFlush(encryptionUtil).onItem().transformToUni(savedOwner -> {
            ApiApp app = new ApiApp();
            app.id = idGenerator.generate(ApiApp.ID_GENERATOR_KEY);
            app.ownerAccountId = savedOwner.id;
            app.clientId = clientId;
            app.clientSecretHash = encryptionUtil.sha256(clientSecret);
            app.name = "Test App";
            app.grantedScopes = scopes;
            app.status = status;
            return app.persistAndFlush().replaceWithVoid();
          });
        });
  }

  private Uni<Void> seedAccount(String email, java.util.function.Consumer<Account> customize) {
    return Account.createNewAnonymousAccount(Locale.ENGLISH, email, idGenerator, encryptionUtil)
        .onItem().transformToUni(account -> {
          account.status = AccountStatus.ACTIVE;
          customize.accept(account);
          AccountEmail accountEmail = account.emails.get(0);
          accountEmail.validated = Instant.now();
          return account.persistAndFlush(encryptionUtil)
              .onItem().transformToUni(saved -> accountEmail.persistAndFlush())
              .replaceWithVoid();
        });
  }
}
