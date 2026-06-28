package app.dissipate.services.grpc;

import app.dissipate.grpc.v1.AccountService;
import app.dissipate.grpc.v1.ValidateSessionRequest;
import app.dissipate.grpc.v1.ValidateSessionResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Verifies the {@link app.dissipate.interceptors.GrpcValidationInterceptor} enforces the
 * {@code (buf.validate.field)} constraints declared on request messages: a request that violates a
 * constraint is rejected with {@code INVALID_ARGUMENT} before the handler runs, while a
 * well-formed request passes validation and reaches the handler.
 */
@QuarkusTest
class GrpcValidationTest {

  private static final Duration TIMEOUT = Duration.ofSeconds(15);

  @GrpcClient("account")
  AccountService accountClient;

  private Status.Code failureCode(ValidateSessionRequest request) {
    UniAssertSubscriber<ValidateSessionResponse> sub = accountClient.validateSession(request)
        .subscribe().withSubscriber(UniAssertSubscriber.create());
    sub.awaitFailure(TIMEOUT);
    Throwable failure = sub.getFailure();
    assertInstanceOf(StatusRuntimeException.class, failure);
    return ((StatusRuntimeException) failure).getStatus().getCode();
  }

  @Test
  void emptySidViolatesConstraint() {
    // sid has (buf.validate.field).string.min_len = 1 — an empty sid must be rejected up front.
    ValidateSessionRequest request = ValidateSessionRequest.newBuilder().setSid("").setOtp("123456").build();
    assertEquals(Status.Code.INVALID_ARGUMENT, failureCode(request));
  }

  @Test
  void wellFormedRequestPassesValidationAndReachesHandler() {
    // Both fields satisfy the constraints, so validation passes; the handler then rejects the
    // unknown session with NOT_FOUND — proving the request was NOT short-circuited as INVALID_ARGUMENT.
    ValidateSessionRequest request = ValidateSessionRequest.newBuilder()
        .setSid(UUID.randomUUID().toString()).setOtp("123456").build();
    assertEquals(Status.Code.NOT_FOUND, failureCode(request));
  }
}
