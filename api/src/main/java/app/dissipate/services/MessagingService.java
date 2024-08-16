package app.dissipate.services;

import app.dissipate.data.models.SessionValidation;
import app.dissipate.grpc.SessionValidationProto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class MessagingService {

  @Inject
  @Channel("session-validation-start-out")
  Emitter<byte[]> sessionValidationEmitter;

  public void startSessionValidation(SessionValidation sessionValidation) {
    // send protos over the wire
    SessionValidationProto sessionValidationProto = SessionValidationProto.newBuilder().setId(sessionValidation.id).build();
    sessionValidationEmitter.send(sessionValidationProto.toByteArray());
  }
}
