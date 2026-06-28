package app.dissipate.services.grpc;

import app.dissipate.constants.AuthenticationConstants;
import app.dissipate.grpc.v1.ChatService;
import app.dissipate.grpc.v1.GetChatsRequest;
import app.dissipate.grpc.v1.GetChatsResponse;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end coverage for the streaming {@code ChatService.GetChats}: an authenticated caller gets
 * the (currently empty) stream, and an unauthenticated caller's stream fails with UNAUTHENTICATED —
 * exercising the auth pipeline gating a Multi-returning method.
 */
@QuarkusTest
class GrpcChatTest {

  private static final Duration TIMEOUT = Duration.ofSeconds(15);

  @GrpcClient("chat")
  ChatService chatClient;

  @Inject
  GrpcAuthTestSeeder seeder;

  @Test
  void authenticatedCallerGetsEmptyStream() throws Throwable {
    String sid = VertxContextSupport.subscribeAndAwait(() -> seeder.seedValidatedSession());
    Metadata md = new Metadata();
    md.put(AuthenticationConstants.AUTHORIZATION_HEADER_KEY, "Bearer " + sid);

    List<GetChatsResponse> chats = GrpcClientUtils.attachHeaders(chatClient, md)
        .getChats(GetChatsRequest.newBuilder().build())
        .collect().asList().await().atMost(TIMEOUT);

    assertTrue(chats.isEmpty());
  }

  @Test
  void unauthenticatedStreamIsRejected() {
    UniAssertSubscriber<List<GetChatsResponse>> sub = chatClient
        .getChats(GetChatsRequest.newBuilder().build())
        .collect().asList()
        .subscribe().withSubscriber(UniAssertSubscriber.create());
    sub.awaitFailure(TIMEOUT);
    Throwable failure = sub.getFailure();
    assertInstanceOf(StatusRuntimeException.class, failure);
    assertEquals(Status.Code.UNAUTHENTICATED, ((StatusRuntimeException) failure).getStatus().getCode());
  }
}
