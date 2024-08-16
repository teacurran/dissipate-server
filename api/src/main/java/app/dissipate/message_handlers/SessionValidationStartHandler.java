package app.dissipate.message_handlers;

import app.dissipate.data.models.SessionValidation;
import app.dissipate.grpc.SessionValidationProto;
import com.google.protobuf.InvalidProtocolBufferException;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.vertx.SafeVertxContext;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class SessionValidationStartHandler {

  private static final Logger LOGGER = Logger.getLogger(SessionValidationStartHandler.class);

  @Inject
  ReactiveMailer mailer;

  @Inject
  Mutiny.SessionFactory factory;

  @SafeVertxContext
  @WithSession
  @Incoming("session-validation-start-in")
  public Uni<Void> handleSessionValidation(byte[] message) {

    try {
      SessionValidationProto sv = SessionValidationProto.parseFrom(message);
      String id = sv.getId();
      LOGGER.info("handleSessionValidation(): " + id);

      return SessionValidation.byId(id).onItem().transformToUni(sessionValidation -> {
        if (sessionValidation == null) {
          LOGGER.error("SessionValidation not found: " + sv.getId());
          return Uni.createFrom().voidItem();
        }

        if (sessionValidation.email != null) {
          Mail m = new Mail();
          m.setFrom("admin@hallofjustice.net");
          m.setTo(List.of(sessionValidation.email.email));
          m.setText("Lex Luthor has been seen in Gotham City!");
          m.setSubject("WARNING: Super Villain Alert");

          LOGGER.info("handleSessionValidation(): " + sessionValidation.email.email);

          return mailer.send(m);
        } else if (sessionValidation.phone != null) {
          LOGGER.info("handleSessionValidation(): " + sessionValidation.phone.phone);
        } else {
          LOGGER.error("SessionValidation has no email or phone: " + id);
        }

        return Uni.createFrom().voidItem();
      }).onFailure().call(t -> {
        LOGGER.error("Error handling session validation", t);
        return Uni.createFrom().voidItem();
      });

    } catch (InvalidProtocolBufferException e) {
      LOGGER.error("Error parsing session validation proto", e);
      return Uni.createFrom().nullItem();
    }
  }
}
