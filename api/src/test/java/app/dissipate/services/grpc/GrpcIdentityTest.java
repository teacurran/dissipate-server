package app.dissipate.services.grpc;

import app.dissipate.constants.AuthenticationConstants;
import app.dissipate.grpc.v1.ChangeIdentityRequest;
import app.dissipate.grpc.v1.ChangeIdentityResponse;
import app.dissipate.grpc.v1.CreateIdentityRequest;
import app.dissipate.grpc.v1.CreateIdentityResponse;
import app.dissipate.grpc.v1.IdentityService;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.grpc.GrpcClientUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * End-to-end coverage for {@code IdentityService} over the wire: a USER-role session can create and
 * switch identities (exercising the auth pipeline + the method handlers), a malformed identity id is
 * rejected, and an unauthenticated caller is refused.
 */
@QuarkusTest
class GrpcIdentityTest {

  private static final Duration TIMEOUT = Duration.ofSeconds(15);
  private static final AtomicInteger COUNTER = new AtomicInteger();

  @GrpcClient("identity")
  IdentityService identityClient;

  @Inject
  GrpcAuthTestSeeder seeder;

  private String sid;

  @BeforeEach
  void seed() throws Throwable {
    sid = VertxContextSupport.subscribeAndAwait(() -> seeder.seedValidatedSession());
  }

  private IdentityService authed() {
    Metadata md = new Metadata();
    md.put(AuthenticationConstants.AUTHORIZATION_HEADER_KEY, "Bearer " + sid);
    return GrpcClientUtils.attachHeaders(identityClient, md);
  }

  private CreateIdentityResponse createIdentity() {
    String username = "ident-" + COUNTER.incrementAndGet() + "-" + System.nanoTime();
    return authed().createIdentity(CreateIdentityRequest.newBuilder()
        .setUsername(username).setName("Test Persona").build()).await().atMost(TIMEOUT);
  }

  @Test
  void createIdentityReturnsThePersona() {
    CreateIdentityResponse response = createIdentity();
    assertEquals(sid, response.getSid());
    assertEquals("Test Persona", response.getName());
    assertFalse(response.getUsername().isBlank());
    assertFalse(response.getIid().isBlank());
  }

  @Test
  void changeIdentityResolvesAnExistingPersona() {
    String iid = createIdentity().getIid();

    ChangeIdentityResponse response = authed()
        .changeIdentity(ChangeIdentityRequest.newBuilder().setIid(iid).build()).await().atMost(TIMEOUT);

    assertEquals(iid, response.getIid());
    assertEquals(sid, response.getSid());
  }

  @Test
  void changeIdentityRejectsUnknownId() {
    // Well-formed base-36 id that maps to no identity -> the not-found branch.
    String unknownIid = Long.toString(987654321L, 36);
    UniAssertSubscriber<ChangeIdentityResponse> sub = authed()
        .changeIdentity(ChangeIdentityRequest.newBuilder().setIid(unknownIid).build())
        .subscribe().withSubscriber(UniAssertSubscriber.create());
    sub.awaitFailure(TIMEOUT);
    Throwable failure = sub.getFailure();
    assertInstanceOf(StatusRuntimeException.class, failure);
    assertEquals(Status.Code.PERMISSION_DENIED, ((StatusRuntimeException) failure).getStatus().getCode());
  }

  @Test
  void changeIdentityRejectsMalformedId() {
    UniAssertSubscriber<ChangeIdentityResponse> sub = authed()
        .changeIdentity(ChangeIdentityRequest.newBuilder().setIid("not-base36-!!!").build())
        .subscribe().withSubscriber(UniAssertSubscriber.create());
    sub.awaitFailure(TIMEOUT);
    Throwable failure = sub.getFailure();
    assertInstanceOf(StatusRuntimeException.class, failure);
    assertEquals(Status.Code.PERMISSION_DENIED, ((StatusRuntimeException) failure).getStatus().getCode());
  }

  @Test
  void createIdentityRequiresAuthentication() {
    UniAssertSubscriber<CreateIdentityResponse> sub = identityClient
        .createIdentity(CreateIdentityRequest.newBuilder().setUsername("x").setName("y").build())
        .subscribe().withSubscriber(UniAssertSubscriber.create());
    sub.awaitFailure(TIMEOUT);
    Throwable failure = sub.getFailure();
    assertInstanceOf(StatusRuntimeException.class, failure);
    assertEquals(Status.Code.UNAUTHENTICATED, ((StatusRuntimeException) failure).getStatus().getCode());
  }
}
