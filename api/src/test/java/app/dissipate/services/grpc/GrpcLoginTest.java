package app.dissipate.services.grpc;

import app.dissipate.constants.AuthenticationConstants;
import app.dissipate.grpc.v1.AccountService;
import app.dissipate.grpc.v1.GetSessionRequest;
import app.dissipate.grpc.v1.GetSessionResponse;
import app.dissipate.grpc.v1.LoginRequest;
import app.dissipate.grpc.v1.LoginResponse;
import app.dissipate.grpc.v1.SessionService;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.grpc.GrpcClientUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * End-to-end check of {@code AccountService.Login} (password login + account lockout) over the wire:
 * a correct password yields a session that authenticates subsequent calls, wrong passwords are
 * rejected generically, and the account locks out after the configured threshold.
 */
@QuarkusTest
class GrpcLoginTest {

  private static final Duration TIMEOUT = Duration.ofSeconds(15);
  private static final String PASSWORD = "correct horse battery staple";
  private static final AtomicInteger COUNTER = new AtomicInteger();

  @GrpcClient("account")
  AccountService accountClient;

  @GrpcClient("session")
  SessionService sessionClient;

  @Inject
  GrpcAuthTestSeeder seeder;

  private String seedAccount() throws Throwable {
    String email = "login-" + COUNTER.incrementAndGet() + "-" + System.nanoTime() + "@example.test";
    VertxContextSupport.subscribeAndAwait(() -> seeder.seedAccountWithPassword(email, PASSWORD));
    return email;
  }

  private LoginResponse login(String email, String password) {
    return accountClient.login(LoginRequest.newBuilder().setEmail(email).setPassword(password).build())
        .await().atMost(TIMEOUT);
  }

  private Status.Code loginFailureCode(String email, String password) {
    UniAssertSubscriber<LoginResponse> sub = accountClient
        .login(LoginRequest.newBuilder().setEmail(email).setPassword(password).build())
        .subscribe().withSubscriber(UniAssertSubscriber.create());
    sub.awaitFailure(TIMEOUT);
    Throwable failure = sub.getFailure();
    assertInstanceOf(StatusRuntimeException.class, failure);
    return ((StatusRuntimeException) failure).getStatus().getCode();
  }

  @Test
  void loginSucceedsAndSessionAuthenticates() throws Throwable {
    String email = seedAccount();

    LoginResponse response = login(email, PASSWORD);
    assertFalse(response.getSid().isBlank());

    // The logged-in session (no OTP validation) must authenticate subsequent calls.
    Metadata md = new Metadata();
    md.put(AuthenticationConstants.AUTHORIZATION_HEADER_KEY, "Bearer " + response.getSid());
    SessionService authed = GrpcClientUtils.attachHeaders(sessionClient, md);
    GetSessionResponse session = authed.getSession(GetSessionRequest.newBuilder().build()).await().atMost(TIMEOUT);
    assertEquals(response.getSid(), session.getSid());
  }

  @Test
  void wrongPasswordIsRejected() throws Throwable {
    String email = seedAccount();
    assertEquals(Status.Code.UNAUTHENTICATED, loginFailureCode(email, "wrong-password"));
  }

  @Test
  void unknownEmailIsRejected() {
    assertEquals(Status.Code.UNAUTHENTICATED,
        loginFailureCode("nobody-" + System.nanoTime() + "@example.test", "whatever"));
  }

  @Test
  void accountLocksOutAfterThreshold() throws Throwable {
    String email = seedAccount();

    // Default max-failed-logins = 5: the first 4 wrong attempts are plain auth failures...
    for (int i = 0; i < 4; i++) {
      assertEquals(Status.Code.UNAUTHENTICATED, loginFailureCode(email, "wrong-password"));
    }
    // ...the 5th trips the lockout...
    assertEquals(Status.Code.RESOURCE_EXHAUSTED, loginFailureCode(email, "wrong-password"));
    // ...and even the correct password is now refused while locked.
    assertEquals(Status.Code.RESOURCE_EXHAUSTED, loginFailureCode(email, PASSWORD));
  }
}
