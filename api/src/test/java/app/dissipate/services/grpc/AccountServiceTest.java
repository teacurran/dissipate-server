package app.dissipate.services.grpc;

import app.dissipate.constants.AuthenticationConstants;
import app.dissipate.grpc.DissipateService;
import app.dissipate.grpc.GetSessionRequest;
import app.dissipate.grpc.GetSessionResponse;
import app.dissipate.grpc.RegisterRequest;
import app.dissipate.grpc.RegisterResponse;
import app.dissipate.grpc.ValidateSessionRequest;
import app.dissipate.grpc.ValidateSessionResponse;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.quarkiverse.mailpit.test.Mailbox;
import io.quarkiverse.mailpit.test.model.Message;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.grpc.GrpcClientUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.callback.QuarkusTestAfterEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestBeforeTestExecutionCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.wildfly.common.Assert.assertTrue;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountServiceTest implements QuarkusTestAfterEachCallback, QuarkusTestBeforeTestExecutionCallback {

  private static final Logger LOGGER = Logger.getLogger(AccountServiceTest.class);

  @GrpcClient("dissipate")
  DissipateService client;

  Mailbox mailbox = new Mailbox() {
    @Override
    public String getMailApiUrl() {
      String mailpitUrl = System.getenv("QUARKUS_MAILPIT_HTTP_SERVER");
      if (mailpitUrl == null) {
        mailpitUrl = "http://localhost:8025";
      }

      return mailpitUrl;
    }
  };

  @Override
  public void afterEach(QuarkusTestMethodContext context) {
    mailbox.clear();
  }

  @Override
  public void beforeTestExecution(QuarkusTestMethodContext context) {
    // do nothing
  }

  @Test
  void shouldAllowAnonymousSessions() {
    CompletableFuture<RegisterResponse> message = new CompletableFuture<>();

    UniAssertSubscriber<RegisterResponse> subscriber = client.register(RegisterRequest.newBuilder().build())
      .subscribe().withSubscriber(UniAssertSubscriber.create());

    RegisterResponse response = subscriber.awaitItem().getItem();
    Assertions.assertEquals("SessionCreated", response.getResult().toString());
    Assertions.assertNotNull(response.getSid());
  }

  @Test
  void shouldValidateEmail(UniAsserter asserter) throws InterruptedException {
    String email = "test-" + new Random().nextInt() + "X-invalid-email.co.uk";

    // one way to test
    // doesn't work with reactive transactions
    UniAssertSubscriber<RegisterResponse> subscriber = client.register(RegisterRequest.newBuilder()
        .setEmail(email).build())
      .subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitFailure().assertFailedWith(StatusRuntimeException.class, "INVALID_ARGUMENT: The email address is invalid.");
  }

  @Test
  void registerByEmail() {
    String email = "create-" + new Random().nextInt() + "@grilledcheese.com";

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
      .onFailure().invoke(throwable -> LOGGER.error("Error: " + throwable.getMessage(), throwable))
      .toUni();

    UniAssertSubscriber<Message> messageSubscriber = uniMessage.subscribe()
      .withSubscriber(UniAssertSubscriber.create());

    Message message = messageSubscriber.awaitItem().getItem();
    assertThat(message, notNullValue());
    assertThat(message.getTo().get(0).getAddress(), is(email));
    assertThat(message.getSubject(), is("One Time Password for Email Verification"));

    String html = message.getHTML();
    assertThat(html, notNullValue());

    // parses the OTP from the email, ie:
    // <span class="otp-box">8QFDFH</span>
    Pattern otpPattern = Pattern.compile("<span class=\"otp-box\">([A-Z0-9]{6})</span>");
    Matcher matcher = otpPattern.matcher(html);
    assertThat(matcher.find(), is(true));
    String emailOtp = matcher.group(1);
    String sid = response.getSid();

    ValidateSessionRequest vsr = ValidateSessionRequest.newBuilder()
      .setSid(sid)
      .setOtp(emailOtp)
      .build();

    UniAssertSubscriber<ValidateSessionResponse> validateSession = client.validateSession(vsr)
      .subscribe().withSubscriber(UniAssertSubscriber.create());

    ValidateSessionResponse validateSessionResponse = validateSession.awaitItem().getItem();
    LOGGER.info("Validation response: " + validateSessionResponse);
    assertTrue(validateSessionResponse.getValid());

    Metadata headers = new Metadata();
    headers.put(AuthenticationConstants.AUTH_HEADER_KEY, sid);
    DissipateService authedClient = GrpcClientUtils.attachHeaders(client, headers);

    UniAssertSubscriber<GetSessionResponse> getSession = authedClient.getSession(GetSessionRequest.newBuilder().build())
      .subscribe().withSubscriber(UniAssertSubscriber.create());

    GetSessionResponse getSessionResponse = getSession.awaitItem().getItem();
    assertThat(getSessionResponse, notNullValue());
    assertThat(getSessionResponse.getSid(), is(sid));
  }

  @Test
  void shouldThrowExceptionWithInvalidToken() {
    CompletableFuture<String> message = new CompletableFuture<>();

    Metadata headers = new Metadata();
    headers.put(AuthenticationConstants.AUTH_HEADER_KEY, "invalid-token");
    DissipateService authedClient = GrpcClientUtils.attachHeaders(client, headers);

    authedClient.getSession(GetSessionRequest.newBuilder().build())
      .onFailure().invoke(message::obtrudeException)
      .subscribe().with(reply -> message.complete(reply.toString()));

    Assertions.assertThrows(ExecutionException.class, () ->
      message.get(5, TimeUnit.SECONDS));
  }

}
