package app.dissipate.services.auth;

import app.dissipate.api.rest.dto.AuthRegisterResponse;
import app.dissipate.api.rest.dto.AuthVerifyResponse;
import app.dissipate.api.rest.error.ApiErrorFactory;
import app.dissipate.api.rest.error.RestErrorCodes;
import app.dissipate.api.rest.i18n.RequestLocale;
import app.dissipate.data.jpa.UuidGenerator;
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
  UuidGenerator uuidGenerator;

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
      return Account.createNewAnonymousAccount(requestLocale.getLocale(), emailToUse, uuidGenerator, encryptionUtil)
        .onItem().transformToUni(account -> {
          Session session = new Session();
          session.account = account;
          session.clientIp = clientIp;
          return session.persistAndFlush().onItem().transformToUni(s -> {
            SessionValidation sv = new SessionValidation();
            sv.id = uuidGenerator.generate();
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
        UUID accountId = sv.session.account != null ? sv.session.account.id : null;

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

  private Uni<AuditEvent> audit(AuditEventType type, AuditOutcome outcome, UUID actorAccountId, UUID sessionId,
                                String targetType, UUID targetId, String clientIp, String userAgent,
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
