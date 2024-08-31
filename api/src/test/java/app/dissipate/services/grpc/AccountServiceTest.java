package app.dissipate.services.grpc;

import app.dissipate.grpc.DissipateService;
import app.dissipate.grpc.RegisterRequest;
import app.dissipate.grpc.RegisterResponse;
import io.quarkiverse.mailpit.test.InjectMailbox;
import io.quarkiverse.mailpit.test.Mailbox;
import io.quarkiverse.mailpit.test.WithMailbox;
import io.quarkiverse.mailpit.test.model.Message;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.TestReactiveTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.callback.QuarkusTestAfterEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestBeforeEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

@QuarkusTest
@WithMailbox
class AccountServiceTest implements QuarkusTestAfterEachCallback, QuarkusTestBeforeEachCallback {

  private static final Logger LOGGER = Logger.getLogger(AccountServiceTest.class);

  @InjectMailbox
  Mailbox mailbox;

  @GrpcClient("dissipate")
  DissipateService client;

  @Override
  public void afterEach(QuarkusTestMethodContext context) {
    mailbox.clear();
  }

  @Override
  public void beforeEach(QuarkusTestMethodContext context) {
    // mailbox.clear();
  }

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
  void registerByEmail(UniAsserter asserter) {
    String email = "create-" + new Random().nextInt() + "@grilledcheese.com";

    //  CompletableFuture<RegisterResponse> message = new CompletableFuture<>();
    //  client.register(RegisterRequest.newBuilder()
    //      .setEmail("tea@grilledcheese.com").build())
    //    .subscribe().with(response -> {
    //      LOGGER.info("Response: " + response);
    //      message.complete(response);
    //    });

    //  asserter.assertThat(
    //    () -> client.register(RegisterRequest.newBuilder().setEmail(email).build()),
    //    (response) -> {
    //      Assertions.assertEquals("EmailSent", response.getResult().toString());
    //      Assertions.assertNotNull(response.getSid());
    //  });

    UniAssertSubscriber<RegisterResponse> subscriber = client.register(RegisterRequest.newBuilder()
        .setEmail(email).build())
      .subscribe().withSubscriber(UniAssertSubscriber.create());

    RegisterResponse response = subscriber.awaitItem().getItem();
    Assertions.assertEquals("EmailSent", response.getResult().toString());
    Assertions.assertNotNull(response.getSid());

    Uni<Message> uniMessage = Multi.createBy()
      .repeating()
      .supplier(() -> mailbox.findFirst(email))
      .withDelay(Duration.ofMillis(500))
      .atMost(4)
      .toUni();

    // why doesn't this work?
    //      .until(value -> {
    //  LOGGER.info("Value: " + value);
    //  return Objects.nonNull(value);
    // }).toUni();

    UniAssertSubscriber<Message> messageSubscriber = uniMessage.subscribe()
      .withSubscriber(UniAssertSubscriber.create());
    Message message = messageSubscriber.awaitItem().getItem();

    assertThat(message, notNullValue());
    assertThat(message.getTo().get(0).getAddress(), is(email));
    assertThat(message.getSubject(), is("One Time Password for Email Verification"));
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
