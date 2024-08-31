package app.dissipate.services.grpc;

import app.dissipate.api.grpc.DissipateServiceImpl;
import app.dissipate.beans.AuthTokenVO;
import app.dissipate.exceptions.ApiException;
import app.dissipate.grpc.DissipateService;
import app.dissipate.grpc.RegisterRequest;
import app.dissipate.grpc.RegisterResponse;
import app.dissipate.services.AuthenticationService;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.grpc.GrpcClientUtils;
import io.quarkus.test.InjectMock;
import io.quarkus.test.TestReactiveTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.common.vertx.VertxContext;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.smallrye.mutiny.subscription.Cancellable;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import io.quarkus.test.vertx.UniAsserter;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static app.dissipate.constants.AuthenticationConstants.AUTH_HEADER_KEY;

@QuarkusTest
class AccountServiceTest {

  private static final Logger LOGGER = Logger.getLogger(AccountServiceTest.class);

  @GrpcClient("dissipate")
  DissipateService client;

  @Test
  void shouldRejectEmptyEmails() {
    CompletableFuture<RegisterResponse> message = new CompletableFuture<>();

    RegisterRequest request = RegisterRequest.newBuilder().build();

    Assertions.assertThrows(ExecutionException.class, () -> {
      client.register(request)
        .onFailure().invoke(message::obtrudeException)
        .subscribe().with(reply -> {
          message.complete(reply);
          throw new RuntimeException("Should not have reached here");
        });

      message.get().wait();
    });


    Assertions.assertThrows(ExecutionException.class, () ->
      message.get(5, TimeUnit.SECONDS));
  }

  @Test
  @TestReactiveTransaction
  void shouldValidateEmail(UniAsserter asserter) throws InterruptedException {
    String email = "test-" + new Random().nextInt() + "X-invalid-email.co.uk";

    // one way to test
    // doesn't work with reactive transactions
    //  UniAssertSubscriber<RegisterResponse> subscriber = client.register(RegisterRequest.newBuilder()
    //      .setEmail(email).build())
    //    .subscribe().withSubscriber(UniAssertSubscriber.create());
    //  subscriber.awaitFailure().assertFailedWith(StatusRuntimeException.class, "INVALID_ARGUMENT: The email address is invalid.");

    // another way to test
    asserter.assertFailedWith(() -> client.register(RegisterRequest.newBuilder().setEmail(email).build()), (cve) -> {
      Assertions.assertEquals("INVALID_ARGUMENT: The email address is invalid.", cve.getMessage());
    });

  }

  @Test
  @TestReactiveTransaction
  void shouldReturnValue(UniAsserter asserter) {
    String email = "create-" + new Random().nextInt() + "@grilledcheese.com";

    //  CompletableFuture<RegisterResponse> message = new CompletableFuture<>();
    //  client.register(RegisterRequest.newBuilder()
    //      .setEmail("tea@grilledcheese.com").build())
    //    .subscribe().with(response -> {
    //      LOGGER.info("Response: " + response);
    //      message.complete(response);
    //    });

    asserter.assertThat(
      () -> client.register(RegisterRequest.newBuilder().setEmail(email).build()),
      (response) -> {
        Assertions.assertEquals("EmailSent", response.getResult().toString());
        Assertions.assertNotNull(response.getSid());
    });
  }

  @Test
  void shouldThrowExceptionWithoutToken() {
    CompletableFuture<String> message = new CompletableFuture<>();

    client.register(RegisterRequest.newBuilder().build())
      .onFailure().invoke(message::obtrudeException)
      .subscribe().with(reply -> message
        .complete(reply.toString())
      );

    Assertions.assertThrows(ExecutionException.class, () ->
      message.get(5, TimeUnit.SECONDS));
  }
}
