package app.dissipate.services.grpc;

import app.dissipate.beans.AuthTokenVO;
import app.dissipate.grpc.DissipateService;
import app.dissipate.grpc.RegisterRequest;
import app.dissipate.services.AuthenticationService;
import io.grpc.Metadata;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.grpc.GrpcClientUtils;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.common.vertx.VertxContext;
import jakarta.inject.Inject;
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
    CompletableFuture<String> message = new CompletableFuture<>();

    RegisterRequest request = RegisterRequest.newBuilder().build();
    Assertions.assertThrows(ExecutionException.class, () -> {
      client.register(request)
        .subscribe().with(reply -> {
          throw new RuntimeException("Should not have reached here");
        });
    });


    Assertions.assertThrows(ExecutionException.class, () ->
      message.get(5, TimeUnit.SECONDS));
  }

  @Test
  void shouldReturnValue() {
    CompletableFuture<String> message = new CompletableFuture<>();

    Metadata extraHeaders = new Metadata();
    extraHeaders.put(AUTH_HEADER_KEY, "test-auth-token");

    DissipateService authedClient = GrpcClientUtils.attachHeaders(client, extraHeaders);

    authedClient.register(RegisterRequest.newBuilder().build())
      .subscribe().with(reply -> message.complete(reply.toString()));
    try {
      String msgValue = message.get(5, TimeUnit.SECONDS);
      Assertions.assertEquals("test-uid", msgValue);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeException(e);
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
