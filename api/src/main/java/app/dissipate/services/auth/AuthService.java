package app.dissipate.services.auth;

import app.dissipate.api.rest.dto.AuthLoginResponse;
import app.dissipate.api.rest.dto.AuthRegisterResponse;
import app.dissipate.api.rest.dto.AuthVerifyResponse;
import app.dissipate.api.rest.error.ApiErrorFactory;
import app.dissipate.api.rest.error.RestErrorCodes;
import app.dissipate.api.rest.i18n.RequestLocale;
import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.models.Account;
import app.dissipate.data.models.AccountEmail;
import app.dissipate.data.models.AccountStatus;
import app.dissipate.data.models.AuditEvent;
import app.dissipate.data.models.AuditEventType;
import app.dissipate.data.models.AuditOutcome;
import app.dissipate.data.models.Session;
import app.dissipate.data.models.SessionValidation;
import app.dissipate.services.DelayedJobService;
import app.dissipate.services.audit.AuditService;
import app.dissipate.utils.EncryptionUtil;
import app.dissipate.utils.StringUtil;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.mail.mailencoder.EmailAddress;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * REST auth/registration logic, mirroring the gRPC RegisterMethod/ValidateSessionMethod flows but
 * with OTP expiry + attempt limits and an audit trail. Must be called within the caller's
 * {@code @WithTransaction}.
 *
 * <p>Methods return {@link Response} (not a thrown exception or failed {@code Uni}) so that error
 * outcomes still <em>commit</em> their transactional side-effects — the attempt counter and the
 * failure audit row — which a rolled-back transaction would otherwise discard. Success returns the
 * typed DTO entity; the error body is the standard {@code ApiErrorResponse} envelope.
 */
@ApplicationScoped
public class AuthService {

  @Inject
  SnowflakeIdGenerator snowflakeIdGenerator;

  @Inject
  EncryptionUtil encryptionUtil;

  @Inject
  DelayedJobService delayedJobService;

  @Inject
  AuditService auditService;

  @Inject
  RequestLocale requestLocale;

  @Inject
  ApiErrorFactory apiErrorFactory;

  /** Consecutive password failures before the account is locked. */
  @ConfigProperty(name = "dissipate.auth.max-failed-logins", defaultValue = "5")
  int maxFailedLogins;

  /** How long an account stays locked after exceeding {@link #maxFailedLogins}. */
  @ConfigProperty(name = "dissipate.auth.lockout-duration", defaultValue = "15m")
  Duration lockoutDuration;

  /** HTTP 429 Too Many Requests — no {@link Response.Status} constant exists for it. */
  private static final int HTTP_TOO_MANY_REQUESTS = 429;

  /**
   * Begin sign-up. With neither email nor phone, creates a bare anonymous session. With an email,
   * creates an anonymous account + pending OTP and queues delivery. Phone is not supported.
   */
  @WithSpan("AuthService.register")
  public Uni<Response> register(String email, String phone, String clientIp, String userAgent) {
    String normEmail = email == null ? "" : email.trim();
    String normPhone = phone == null ? "" : phone.trim();

    if (normEmail.isEmpty() && normPhone.isEmpty()) {
      Session session = new Session();
      session.clientIp = clientIp;
      return session.persistAndFlush().onItem().transformToUni(s ->
        audit(AuditEventType.AUTH_REGISTER, AuditOutcome.SUCCESS, null, s.id, "Session", null,
          clientIp, userAgent, null, "{\"channel\":\"session\"}")
          .onItem().transform(a -> ok(new AuthRegisterResponse(s.id.toString(), "SESSION_CREATED"))));
    }

    if (normEmail.isEmpty()) {
      return Uni.createFrom().item(error(Response.Status.NOT_IMPLEMENTED, RestErrorCodes.AUTH_PHONE_UNSUPPORTED));
    }

    final String emailToUse;
    try {
      emailToUse = normalizeEmail(normEmail);
    } catch (IllegalArgumentException e) {
      return Uni.createFrom().item(error(Response.Status.BAD_REQUEST, RestErrorCodes.AUTH_EMAIL_INVALID));
    }

    return AccountEmail.findByValidatedEmail(emailToUse).onItem().transformToUni(existing -> {
      if (existing != null) {
        return audit(AuditEventType.AUTH_REGISTER, AuditOutcome.FAILURE,
          existing.account != null ? existing.account.id : null, null, "AccountEmail", existing.id,
          clientIp, userAgent, "email_exists", "{\"channel\":\"email\"}")
          .onItem().transform(a -> error(Response.Status.CONFLICT, RestErrorCodes.AUTH_EMAIL_EXISTS));
      }
      return Account.createNewAnonymousAccount(requestLocale.getLocale(), emailToUse, snowflakeIdGenerator, encryptionUtil)
        .onItem().transformToUni(account -> {
          Session session = new Session();
          session.account = account;
          session.clientIp = clientIp;
          return session.persistAndFlush().onItem().transformToUni(s -> {
            SessionValidation sv = new SessionValidation();
            sv.id = snowflakeIdGenerator.generate(SessionValidation.ID_GENERATOR_KEY);
            sv.session = s;
            sv.email = account.emails.get(0);
            sv.token = StringUtil.generateRandomString(SessionValidation.OTP_LENGTH);
            sv.expires = Instant.now().plus(SessionValidation.OTP_TTL);
            sv.attempts = 0;
            return sv.persistAndFlush()
              .onItem().transformToUni(saved -> delayedJobService.createDelayedJob(saved))
              .onItem().transformToUni(dj -> audit(AuditEventType.AUTH_REGISTER, AuditOutcome.SUCCESS,
                account.id, s.id, "AccountEmail", sv.email.id, clientIp, userAgent, null, "{\"channel\":\"email\"}"))
              .onItem().transform(a -> ok(new AuthRegisterResponse(s.id.toString(), "EMAIL_SENT")));
          });
        });
    });
  }

  /**
   * Verify an OTP. Counts attempts and enforces expiry even on a wrong code; on success activates
   * the email (and an anonymous account) and returns the session id as the usable Bearer token.
   */
  @WithSpan("AuthService.verify")
  public Uni<Response> verify(String sid, String otp, String clientIp, String userAgent) {
    String s = sid == null ? "" : sid.trim();
    String code = otp == null ? "" : otp.trim();
    if (s.isEmpty() || code.isEmpty()) {
      return Uni.createFrom().item(error(Response.Status.BAD_REQUEST, RestErrorCodes.AUTH_OTP_INVALID));
    }

    return Uni.createFrom().deferred(() -> SessionValidation.findPendingBySession(s))
      .onFailure(IllegalArgumentException.class).recoverWithItem((SessionValidation) null)
      .onItem().transformToUni(sv -> {
        Instant now = Instant.now();
        if (sv == null) {
          return audit(AuditEventType.AUTH_VERIFY, AuditOutcome.FAILURE, null, null, "Session", null,
            clientIp, userAgent, "no_pending_otp", null)
            .onItem().transform(a -> error(Response.Status.BAD_REQUEST, RestErrorCodes.AUTH_OTP_INVALID));
        }

        UUID sessionId = sv.session.id;
        Long accountId = sv.session.account != null ? sv.session.account.id : null;

        if (sv.isExpired(now) || sv.attempts >= SessionValidation.MAX_OTP_ATTEMPTS) {
          return audit(AuditEventType.AUTH_VERIFY, AuditOutcome.FAILURE, accountId, sessionId, "SessionValidation",
            sv.id, clientIp, userAgent, "expired_or_exhausted", null)
            .onItem().transform(a -> error(Response.Status.BAD_REQUEST, RestErrorCodes.AUTH_OTP_INVALID));
        }

        if (!sv.token.equalsIgnoreCase(code)) {
          sv.attempts = sv.attempts + 1;
          return sv.persistAndFlush()
            .onItem().transformToUni(x -> audit(AuditEventType.AUTH_VERIFY, AuditOutcome.FAILURE, accountId, sessionId,
              "SessionValidation", sv.id, clientIp, userAgent, "wrong_code", null))
            .onItem().transform(a -> error(Response.Status.BAD_REQUEST, RestErrorCodes.AUTH_OTP_INVALID));
        }

        String existingEmail = sv.email != null ? sv.email.email : null;
        return checkForExistingValidatedEmail(existingEmail).onItem().transformToUni(conflict -> {
          if (Boolean.TRUE.equals(conflict)) {
            return audit(AuditEventType.AUTH_VERIFY, AuditOutcome.FAILURE, accountId, sessionId, "SessionValidation",
              sv.id, clientIp, userAgent, "email_already_validated", null)
              .onItem().transform(a -> error(Response.Status.CONFLICT, RestErrorCodes.AUTH_EMAIL_EXISTS));
          }
          return markEmailValidated(sv)
            .onItem().transformToUni(v -> {
              sv.validated = now;
              return sv.session.persistAndFlush();
            })
            .onItem().transformToUni(saved -> audit(AuditEventType.AUTH_VERIFY, AuditOutcome.SUCCESS, accountId, sessionId,
              "AccountEmail", sv.email != null ? sv.email.id : null, clientIp, userAgent, null, "{\"channel\":\"email\"}"))
            .onItem().transform(a -> ok(new AuthVerifyResponse(sessionId.toString())));
        });
      });
  }

  /**
   * Password login. Returns a generic failure for unknown email / no password / wrong password (no
   * account enumeration), counts consecutive failures, and locks the account for
   * {@link #lockoutDuration} after {@link #maxFailedLogins}. On success rehashes a legacy hash if
   * needed, clears the lockout, and creates a fresh logged-in session whose id is the Bearer token.
   */
  @WithSpan("AuthService.login")
  public Uni<Response> login(String email, String password, String clientIp, String userAgent) {
    String normEmail = email == null ? "" : email.trim().toLowerCase();
    if (normEmail.isEmpty() || password == null || password.isEmpty()) {
      return Uni.createFrom().item(error(Response.Status.UNAUTHORIZED, RestErrorCodes.AUTH_LOGIN_FAILED));
    }

    return AccountEmail.findByValidatedEmailWithAccount(normEmail).onItem().transformToUni(ae -> {
      Account account = ae != null ? ae.account : null;
      Instant now = Instant.now();

      if (account == null || (account.passwordHashStr == null && account.passwordHash == null)) {
        return audit(AuditEventType.AUTH_LOGIN_FAILED, AuditOutcome.FAILURE,
          account != null ? account.id : null, null, "AccountEmail", ae != null ? ae.id : null,
          clientIp, userAgent, "no_account_or_password", null)
          .onItem().transform(a -> error(Response.Status.UNAUTHORIZED, RestErrorCodes.AUTH_LOGIN_FAILED));
      }

      if (account.isLocked(now)) {
        return audit(AuditEventType.AUTH_LOCKOUT, AuditOutcome.FAILURE, account.id, null, "Account", account.id,
          clientIp, userAgent, "locked", null)
          .onItem().transform(a -> apiErrorFactory.response(HTTP_TOO_MANY_REQUESTS, RestErrorCodes.AUTH_ACCOUNT_LOCKED));
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
        return account.persistAndFlush(encryptionUtil)
          .onItem().transformToUni(a -> audit(type, AuditOutcome.FAILURE, account.id, null, "Account", account.id,
            clientIp, userAgent, reason, null))
          .onItem().transform(a -> nowLocked
            ? apiErrorFactory.response(HTTP_TOO_MANY_REQUESTS, RestErrorCodes.AUTH_ACCOUNT_LOCKED)
            : error(Response.Status.UNAUTHORIZED, RestErrorCodes.AUTH_LOGIN_FAILED));
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
          audit(AuditEventType.AUTH_LOGIN, AuditOutcome.SUCCESS, account.id, s.id, "Session", null,
            clientIp, userAgent, null, "{\"method\":\"password\"}")
            .onItem().transform(a2 -> ok(new AuthLoginResponse(s.id.toString()))));
      });
    });
  }

  /**
   * Set or change the current account's password. When a password already exists, the correct
   * {@code currentPassword} must be supplied. Clears any lockout on success.
   */
  @WithSpan("AuthService.setPassword")
  public Uni<Response> setPassword(Account account, String currentPassword, String newPassword,
                                   String clientIp, String userAgent) {
    boolean hasExisting = account.passwordHashStr != null || account.passwordHash != null;
    AuditEventType type = hasExisting ? AuditEventType.AUTH_PASSWORD_CHANGE : AuditEventType.AUTH_PASSWORD_SET;

    if (hasExisting) {
      if (currentPassword == null || currentPassword.isEmpty()
        || !account.verifyPassword(encryptionUtil, currentPassword).matched()) {
        return audit(type, AuditOutcome.FAILURE, account.id, null, "Account", account.id,
          clientIp, userAgent, "wrong_current_password", null)
          .onItem().transform(a -> error(Response.Status.FORBIDDEN, RestErrorCodes.AUTH_PASSWORD_INVALID));
      }
    }

    account.password = newPassword; // hashed by persistAndFlush(encryptionUtil)
    account.failedLoginAttempts = 0;
    account.lockedUntil = null;
    return account.persistAndFlush(encryptionUtil)
      .onItem().transformToUni(a -> audit(type, AuditOutcome.SUCCESS, account.id, null, "Account", account.id,
        clientIp, userAgent, null, null))
      .onItem().transform(a -> Response.noContent().build());
  }

  /** End the given session and audit the logout. */
  @WithSpan("AuthService.logout")
  public Uni<Response> logout(Session session, String clientIp, String userAgent) {
    session.ended = Instant.now();
    Long accountId = session.account != null ? session.account.id : null;
    return session.persistAndFlush()
      .onItem().transformToUni(s -> audit(AuditEventType.AUTH_LOGOUT, AuditOutcome.SUCCESS, accountId, session.id,
        "Session", null, clientIp, userAgent, null, null))
      .onItem().transform(a -> Response.noContent().build());
  }

  // ---- helpers -------------------------------------------------------------

  private String normalizeEmail(String email) {
    String formatted = email.toLowerCase().trim();
    if (formatted.isEmpty()) {
      throw new IllegalArgumentException("empty email");
    }
    new EmailAddress(formatted); // throws IllegalArgumentException on malformed address
    return formatted;
  }

  /** True if another account already owns this validated email (would fork the identity). */
  private Uni<Boolean> checkForExistingValidatedEmail(String email) {
    if (email == null) {
      return Uni.createFrom().item(false);
    }
    return AccountEmail.findByValidatedEmail(email).onItem().transform(existing -> existing != null);
  }

  /** Mark the OTP's email validated and promote an anonymous account to active. */
  private Uni<Void> markEmailValidated(SessionValidation sv) {
    if (sv.email == null) {
      return Uni.createFrom().voidItem();
    }
    return sv.email.markValidated().onItem().transformToUni(accountEmail -> {
      if (accountEmail.account != null && AccountStatus.ANONYMOUS.equals(accountEmail.account.status)) {
        accountEmail.account.status = AccountStatus.ACTIVE;
        return accountEmail.account.persistAndFlush(encryptionUtil).replaceWithVoid();
      }
      return Uni.createFrom().voidItem();
    });
  }

  private Uni<AuditEvent> audit(AuditEventType type, AuditOutcome outcome, Long actorAccountId, UUID sessionId,
                                String targetType, Long targetId, String clientIp, String userAgent,
                                String reason, String metadata) {
    AuditEvent event = new AuditEvent();
    event.eventType = type;
    event.outcome = outcome;
    event.actorAccountId = actorAccountId;
    event.sessionId = sessionId;
    event.targetType = targetType;
    event.targetId = targetId;
    event.clientIp = clientIp;
    event.userAgent = userAgent;
    event.reason = reason;
    event.metadata = metadata;
    return auditService.record(event);
  }

  private Response ok(Object entity) {
    return Response.ok(entity).build();
  }

  private Response error(Response.Status status, String code) {
    return apiErrorFactory.response(status, code);
  }
}
