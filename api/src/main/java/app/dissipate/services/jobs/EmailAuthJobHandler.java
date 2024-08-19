package app.dissipate.services.jobs;

import app.dissipate.data.models.SessionValidation;
import app.dissipate.exceptions.DelayedJobException;
import app.dissipate.services.LocalizationService;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.CheckedTemplate;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    public static native MailTemplate.MailTemplateInstance otp(ResourceBundle i18n,
                                                               String otp,
                                                               String email);
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

        try {
          byte[] image1 = loadResourceAsBytes("images/image-1.png");
          byte[] image2 = loadResourceAsBytes("images/image-2.png");
          byte[] image3 = loadResourceAsBytes("images/image-3.png");
          byte[] image4 = loadResourceAsBytes("images/image-4.png");
          byte[] image5 = loadResourceAsBytes("images/image-5.png");
          byte[] image6 = loadResourceAsBytes("images/image-6.png");
          byte[] image7 = loadResourceAsBytes("images/image-7.png");
          byte[] image8 = loadResourceAsBytes("images/image-8.png");

          return Templates.otp(localizationService.getBundle(Locale.getDefault()),
              sessionValidation.token,
              sessionValidation.email.email)
            .from("admin@hallofjustice.net")
            .to(sessionValidation.email.email)
            .subject("OTP for Email Verification")
            .addInlineAttachment("image-1.png", image1, "image/png", "image-1.png")
            .addInlineAttachment("image-2.png", image2, "image/png", "image-2.png")
            .addInlineAttachment("image-3.png", image3, "image/png", "image-3.png")
            .addInlineAttachment("image-4.png", image4, "image/png", "image-4.png")
            .addInlineAttachment("image-5.png", image5, "image/png", "image-5.png")
            .addInlineAttachment("image-6.png", image6, "image/png", "image-6.png")
            .addInlineAttachment("image-7.png", image7, "image/png", "image-7.png")
            .addInlineAttachment("image-8.png", image8, "image/png", "image-8.png")
            .send();

//        Mail m = new Mail();
//        m.setFrom("admin@hallofjustice.net");
//        m.setTo(List.of(sessionValidation.email.email));
//        m.setText("Lex Luthor has been seen in Gotham City!");
//        m.setSubject("WARNING: Super Villain Alert");

        } catch (IOException | URISyntaxException e) {
          LOGGER.error("Failed to load image resources", e);
          return Uni.createFrom().failure(new DelayedJobException(true, "Failed to load image resources"));
        }

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

  private byte[] loadResourceAsBytes(String resourcePath) throws IOException, URISyntaxException {
    return Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(resourcePath).toURI()));
  }
}
