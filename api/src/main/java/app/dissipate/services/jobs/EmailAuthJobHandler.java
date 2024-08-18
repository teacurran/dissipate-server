package app.dissipate.services.jobs;

import app.dissipate.data.models.SessionValidation;
import app.dissipate.exceptions.DelayedJobException;
import app.dissipate.services.LocalizationService;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.CheckedTemplate;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

@ApplicationScoped
public class EmailAuthJobHandler implements DelayedJobHandler {
  private static final Logger LOGGER = Logger.getLogger(EmailAuthJobHandler.class);

  @Inject
  ReactiveMailer mailer;

  @Inject
  LocalizationService localizationService;

  @CheckedTemplate
  static class Templates {
    public static native MailTemplate.MailTemplateInstance otp(ResourceBundle i18n);
  }

  @Override
  @WithSpan("EmailAuthJobHandler.run")
  public Uni<Void> run(String actorId) {
    return SessionValidation.byId(actorId).onItem().transformToUni(sessionValidation -> {
      if (sessionValidation == null) {
        LOGGER.error("SessionValidation not found: " + actorId);
        return Uni.createFrom().voidItem();
      }

      if (sessionValidation.email != null) {
        Span.current().setAttribute("email", sessionValidation.email.email);

        LOGGER.info("handleSessionValidation(): " + sessionValidation.email.email);

        return Templates.otp(localizationService.getBundle(Locale.getDefault()))
          .data("otp", sessionValidation.token)
          .data("email", sessionValidation.email.email)
          .from("admin@hallofjustice.net")
          .to(sessionValidation.email.email)
          .subject("OTP for Email Verification")
          .send();

//        Mail m = new Mail();
//        m.setFrom("admin@hallofjustice.net");
//        m.setTo(List.of(sessionValidation.email.email));
//        m.setText("Lex Luthor has been seen in Gotham City!");
//        m.setSubject("WARNING: Super Villain Alert");


      } else if (sessionValidation.phone != null) {
        LOGGER.info("handleSessionValidation(): " + sessionValidation.phone.phone);
      } else {
        LOGGER.error("SessionValidation has no email or phone: " + actorId);
      }

      return Uni.createFrom().voidItem();
    }).onFailure(IllegalArgumentException.class).recoverWithUni(t -> {
      return Uni.createFrom().failure(new DelayedJobException(true, t.getMessage()));
    });
  }
}
