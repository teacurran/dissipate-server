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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
                                                               String email,
                                                               String stylesheet,
                                                               String imageHead);
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

        ResourceBundle i18n = localizationService.getBundle(Locale.getDefault());

        try {
          String imageHead = loadResourceAsBase64("images/server_farm_2_m.png");
          String css = loadResourceAsString("css/email.css");

          return Templates.otp(i18n,
              sessionValidation.token,
              sessionValidation.email.email,
              css,
              imageHead)
            .from("admin@hallofjustice.net")
            .to(sessionValidation.email.email)
            .subject(i18n.getString("auth.email.otp.subject"))
            .send();
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

  private String loadResourceAsString(String resourcePath) throws IOException, URISyntaxException {
    return Files.readAllLines(
      Paths.get(getClass().getClassLoader().getResource(resourcePath).toURI()), StandardCharsets.UTF_8
    ).stream().collect(Collectors.joining("\n"));
  }

  private byte[] loadResourceAsBytes(String resourcePath) throws IOException, URISyntaxException {
    return Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(resourcePath).toURI()));
  }
  public String loadResourceAsBase64(String resourcePath) throws IOException, URISyntaxException {
    byte resource[] = loadResourceAsBytes(resourcePath);
    return java.util.Base64.getEncoder().encodeToString(resource);
  }
}
