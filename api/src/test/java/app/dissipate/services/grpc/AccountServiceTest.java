package app.dissipate.services.grpc;

import app.dissipate.api.grpc.DissipateServiceImpl;
import app.dissipate.beans.AuthTokenVO;
import app.dissipate.exceptions.ApiException;
import app.dissipate.grpc.DissipateService;
import app.dissipate.grpc.RegisterRequest;
import app.dissipate.grpc.RegisterResponse;
import app.dissipate.services.AuthenticationService;
import io.grpc.Metadata;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.grpc.GrpcClientUtils;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.common.vertx.VertxContext;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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

  @InjectMock
  AuthenticationService mockAuth;

  @BeforeEach
  public void setup() {
    AuthTokenVO token = new AuthTokenVO();
    token.setUid("test-uid");
    Mockito.when(mockAuth.verifyIdToken("test-auth-token")).thenReturn(token);
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
  void shouldReturnValue() {
    CompletableFuture<RegisterResponse> message = new CompletableFuture<>();

    Metadata extraHeaders = new Metadata();
    extraHeaders.put(AUTH_HEADER_KEY, "test-auth-token");

    DissipateService authedClient = GrpcClientUtils.attachHeaders(client, extraHeaders);

    authedClient.register(RegisterRequest.newBuilder()
        .setEmail("tea@grilledcheese.com").build())
      .subscribe().with(response -> {
        LOGGER.info("Response: " + response);
        message.complete(response);
      });

    try {
      RegisterResponse response = message.get(5, TimeUnit.SECONDS);
      Assertions.assertEquals("EmailSent", response.getResult().toString());
      Assertions.assertNotNull(response.getSid());
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeException(e);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
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
