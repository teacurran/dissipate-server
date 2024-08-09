package app.dissipate.message_handlers;

import app.dissipate.data.models.SessionValidation;
import app.dissipate.grpc.SessionValidationProto;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SessionValidationStartHandler {

  private static final Logger LOGGER = Logger.getLogger(SessionValidationStartHandler.class);

  @Incoming("session-validation-start-in")
  @Transactional
  @Blocking
  public void handleSessionValidation(byte[] message) {
    try {
      SessionValidationProto sv = SessionValidationProto.parseFrom(message);
      LOGGER.info("handleSessionValidation(): " + sv.getId());
    } catch (Exception e) {
      LOGGER.error("Error parsing session validation message: " + e.getMessage());
      return;
    }

    //LOGGER.info("handleSessionValidation(): " + sessionValidation.email.email);
//    AccountService accountService = new AccountService();
//    accountService.validateSession(sessionValidation);

  }
}
