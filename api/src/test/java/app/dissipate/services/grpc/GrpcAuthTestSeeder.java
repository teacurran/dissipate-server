package app.dissipate.services.grpc;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.models.Account;
import app.dissipate.data.models.AccountStatus;
import app.dissipate.data.models.Session;
import app.dissipate.data.models.SessionValidation;
import app.dissipate.utils.EncryptionUtil;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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

  /** Create an ACTIVE account + session + validated SessionValidation; returns the session sid. */
  @WithTransaction
  public Uni<String> seedValidatedSession() {
    return Account.createNewAnonymousAccount(Locale.ENGLISH, idGenerator, encryptionUtil)
        .onItem().transformToUni(account -> {
          account.status = AccountStatus.ACTIVE;
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
}
