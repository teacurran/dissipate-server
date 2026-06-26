package app.dissipate.interceptors;

import build.buf.protovalidate.Validator;
import build.buf.protovalidate.ValidatorFactory;
import build.buf.protovalidate.ValidationResult;
import build.buf.protovalidate.exceptions.ValidationException;
import com.google.protobuf.Message;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.quarkus.grpc.GlobalInterceptor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.Prioritized;

/**
 * Enforces the {@code (buf.validate.field)} constraints declared in the protos: every inbound
 * request message is run through protovalidate and rejected with {@code INVALID_ARGUMENT} (listing
 * the violations) before it reaches the handler. Priority sits below the authn interceptor so an
 * unauthenticated call is rejected before its payload is validated.
 */
@GlobalInterceptor
@ApplicationScoped
public class GrpcValidationInterceptor implements ServerInterceptor, Prioritized {

  /** Runs after authn (so unauthenticated calls reject first). Ordered via Prioritized. */
  public static final int PRIORITY = 75;

  private final Validator validator = ValidatorFactory.newBuilder().build();

  @Override
  public int getPriority() {
    return PRIORITY;
  }

  @SuppressWarnings("java:S119")
  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {

    ServerCall.Listener<ReqT> delegate = next.startCall(call, metadata);

    return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(delegate) {
      private boolean aborted;

      @Override
      public void onMessage(ReqT message) {
        if (message instanceof Message protoMessage) {
          try {
            ValidationResult result = validator.validate(protoMessage);
            if (!result.isSuccess()) {
              aborted = true;
              call.close(Status.INVALID_ARGUMENT
                  .withDescription("request validation failed: " + result.getViolations()), new Metadata());
              return;
            }
          } catch (ValidationException e) {
            aborted = true;
            call.close(Status.INTERNAL.withDescription("request validation error"), new Metadata());
            return;
          }
        }
        super.onMessage(message);
      }

      @Override
      public void onHalfClose() {
        // The call was already closed in onMessage; do not let the handler run / respond again.
        if (aborted) {
          return;
        }
        super.onHalfClose();
      }
    };
  }
}
