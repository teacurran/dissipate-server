package app.dissipate.services.grpc;

import app.dissipate.grpc.IDissipateService;
import app.dissipate.grpc.RegisterRequest;
import app.dissipate.services.AuthenticationService;
import io.grpc.Metadata;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.grpc.GrpcClientUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
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

    @GrpcClient
    IDissipateService client;

    @InjectMock
    AuthenticationService mockAuth;

    @BeforeEach
    public void setup() {
        Mockito.when(mockAuth.verifyIdToken("test-auth-token")).thenReturn("test-uid");
    }

    @Test
    void shouldReturnValue() {
        CompletableFuture<String> message = new CompletableFuture<>();

        Metadata extraHeaders = new Metadata();
        extraHeaders.put(AUTH_HEADER_KEY, "test-auth-token");

        IDissipateService authedClient = GrpcClientUtils.attachHeaders(client, extraHeaders);

        authedClient.register(RegisterRequest.newBuilder().build())
                .subscribe().with(reply -> message.complete(reply.getAccount().getId()));
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

        client.register(RegisterRequest.newBuilder().build()).onFailure().invoke(message::obtrudeException)
                .subscribe().with(reply -> message
                        .complete(reply.getAccount().getId())
                );

        Assertions.assertThrows(ExecutionException.class, () ->
                message.get(5, TimeUnit.SECONDS));
    }
}
