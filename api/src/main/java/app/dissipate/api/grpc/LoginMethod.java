package app.dissipate.api.grpc;

import app.dissipate.data.models.Account;
import app.dissipate.data.models.AccountEmail;
import app.dissipate.data.models.AuditEvent;
import app.dissipate.data.models.AuditEventType;
import app.dissipate.data.models.AuditOutcome;
import app.dissipate.data.models.Session;
import app.dissipate.grpc.v1.LoginRequest;
import app.dissipate.grpc.v1.LoginResponse;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import app.dissipate.interceptors.GrpcSecurityInterceptor;
import app.dissipate.services.LocalizationService;
import app.dissipate.services.audit.AuditService;
import app.dissipate.utils.EncryptionUtil;
import io.grpc.Status;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_ACCOUNT_LOCKED;
import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_LOGIN_FAILED;

/**
 * Password login over gRPC (salvaged from the retired REST AuthService). Returns a generic failure
 * for unknown email / no password / wrong password (no account enumeration), counts consecutive
 * failures, and locks the account for {@link #lockoutDuration} after {@link #maxFailedLogins}. On
 * success it rehashes a legacy hash if needed, clears the lockout, and opens a fresh logged-in
 * session whose id is the Bearer token for subsequent calls.
 *
 * <p>Runs under {@code @WithSession} (not {@code @WithTransaction}) deliberately: each
 * {@code persistAndFlush} autocommits, so the failed-attempt counter and lockout still persist when
 * the method then returns a failed {@link Uni} — a rolled-back transaction would discard them.
 */
@ApplicationScoped
public class LoginMethod {

  @Inject
  EncryptionUtil encryptionUtil;

  @Inject
  LocalizationService localizationService;

  @Inject
  AuditService auditService;

  /** Consecutive password failures before the account is locked. */
  @ConfigProperty(name = "dissipate.auth.max-failed-logins", defaultValue = "5")
  int maxFailedLogins;

  /** How long an account stays locked after exceeding {@link #maxFailedLogins}. */
  @ConfigProperty(name = "dissipate.auth.lockout-duration", defaultValue = "15m")
  Duration lockoutDuration;

  @WithSpan("LoginMethod.login")
  public Uni<LoginResponse> login(LoginRequest request) {
    final Locale locale = currentLocale();
    final String clientIp = GrpcSecurityInterceptor.CLIENT_IP_KEY.get();
    final String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();
    final String password = request.getPassword();

    if (email.isEmpty() || password == null || password.isEmpty()) {
      return fail(locale, Status.UNAUTHENTICATED, AUTH_LOGIN_FAILED);
    }

    return AccountEmail.findByValidatedEmailWithAccount(email).onItem().transformToUni(ae -> {
      final Account account = ae != null ? ae.account : null;
      final Instant now = Instant.now();

      if (account == null || (account.passwordHashStr == null && account.passwordHash == null)) {
        return audit(AuditEventType.AUTH_LOGIN_FAILED, AuditOutcome.FAILURE,
            account != null ? account.id : null, null, "AccountEmail", ae != null ? ae.id : null,
            clientIp, "no_account_or_password")
            .onItem().transformToUni(a -> fail(locale, Status.UNAUTHENTICATED, AUTH_LOGIN_FAILED));
      }

      if (account.isLocked(now)) {
        return audit(AuditEventType.AUTH_LOCKOUT, AuditOutcome.FAILURE, account.id, null, "Account", account.id,
            clientIp, "locked")
            .onItem().transformToUni(a -> fail(locale, Status.RESOURCE_EXHAUSTED, AUTH_ACCOUNT_LOCKED));
      }

      EncryptionUtil.VerifyResult result = account.verifyPassword(encryptionUtil, password);
      if (!result.matched()) {
        account.failedLoginAttempts = account.failedLoginAttempts + 1;
        boolean nowLocked = account.failedLoginAttempts >= maxFailedLogins;
        if (nowLocked) {
          account.lockedUntil = now.plus(lockoutDuration);
        }
        AuditEventType type = nowLocked ? AuditEventType.AUTH_LOCKOUT : AuditEventType.AUTH_LOGIN_FAILED;
        String reason = nowLocked ? "locked_out" : "wrong_password";
        Status status = nowLocked ? Status.RESOURCE_EXHAUSTED : Status.UNAUTHENTICATED;
        String code = nowLocked ? AUTH_ACCOUNT_LOCKED : AUTH_LOGIN_FAILED;
        return account.persistAndFlush(encryptionUtil)
            .onItem().transformToUni(a -> audit(type, AuditOutcome.FAILURE, account.id, null, "Account", account.id,
                clientIp, reason))
            .onItem().transformToUni(a -> fail(locale, status, code));
      }

      // Success: rehash legacy hashes, clear lockout, open a new logged-in session.
      if (result.needsRehash()) {
        account.rehashPassword(encryptionUtil, password);
      }
      account.failedLoginAttempts = 0;
      account.lockedUntil = null;
      return account.persistAndFlush(encryptionUtil).onItem().transformToUni(a -> {
        Session session = new Session();
        session.account = account;
        session.loggedIn = true;
        session.clientIp = clientIp;
        return session.persistAndFlush().onItem().transformToUni(s ->
            audit(AuditEventType.AUTH_LOGIN, AuditOutcome.SUCCESS, account.id, s.id, "Session", null, clientIp, null)
                .onItem().transform(a2 -> LoginResponse.newBuilder().setSid(s.id.toString()).build()));
      });
    });
  }

  private <T> Uni<T> fail(Locale locale, Status status, String code) {
    return Uni.createFrom().failure(localizationService.getApiException(locale, status, code));
  }

  private Uni<AuditEvent> audit(AuditEventType type, AuditOutcome outcome, Long actorAccountId,
                                java.util.UUID sessionId, String targetType, Long targetId,
                                String clientIp, String reason) {
    AuditEvent event = new AuditEvent();
    event.eventType = type;
    event.outcome = outcome;
    event.actorAccountId = actorAccountId;
    event.sessionId = sessionId;
    event.targetType = targetType;
    event.targetId = targetId;
    event.clientIp = clientIp;
    event.reason = reason;
    return auditService.record(event);
  }

  private static Locale currentLocale() {
    Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();
    return locale != null ? locale : Locale.ENGLISH;
  }
}
