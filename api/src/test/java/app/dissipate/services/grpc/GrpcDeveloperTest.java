package app.dissipate.services.grpc;

import app.dissipate.constants.AuthenticationConstants;
import app.dissipate.grpc.v1.AppSummary;
import app.dissipate.grpc.v1.DeveloperService;
import app.dissipate.grpc.v1.ListAppsRequest;
import app.dissipate.grpc.v1.ListAppsResponse;
import app.dissipate.grpc.v1.RegisterAppRequest;
import app.dissipate.grpc.v1.RegisterAppResponse;
import app.dissipate.grpc.v1.RotateSecretRequest;
import app.dissipate.grpc.v1.RotateSecretResponse;
import app.dissipate.grpc.v1.SetScopesRequest;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.grpc.GrpcClientUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end coverage for {@code DeveloperService}: a verified owner can register, list, rotate the
 * secret of, and re-scope their apps; unknown scopes are rejected; another/absent app id is not
 * found; and a non-verified user is denied by the auth pipeline.
 */
@QuarkusTest
class GrpcDeveloperTest {

  private static final Duration TIMEOUT = Duration.ofSeconds(15);

  @GrpcClient("developer")
  DeveloperService developerClient;

  @Inject
  GrpcAuthTestSeeder seeder;

  private DeveloperService asOwner(String sid) {
    Metadata md = new Metadata();
    md.put(AuthenticationConstants.AUTHORIZATION_HEADER_KEY, "Bearer " + sid);
    return GrpcClientUtils.attachHeaders(developerClient, md);
  }

  private DeveloperService asVerifiedOwner() throws Throwable {
    return asOwner(VertxContextSupport.subscribeAndAwait(() -> seeder.seedVerifiedSession()));
  }

  private static Status.Code failureCode(Uni<?> uni) {
    UniAssertSubscriber<?> sub = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
    sub.awaitFailure(TIMEOUT);
    Throwable failure = sub.getFailure();
    assertInstanceOf(StatusRuntimeException.class, failure);
    return ((StatusRuntimeException) failure).getStatus().getCode();
  }

  @Test
  void registerListRotateAndSetScopes() throws Throwable {
    DeveloperService dev = asVerifiedOwner();

    RegisterAppResponse registered = dev.registerApp(RegisterAppRequest.newBuilder()
        .setName("My App").addScopes("posts:read").addScopes("posts:write").build()).await().atMost(TIMEOUT);
    String appId = registered.getApp().getId();
    assertFalse(registered.getClientSecret().isBlank());
    assertFalse(registered.getApp().getClientId().isBlank());
    assertEquals(List.of("posts:read", "posts:write"), registered.getApp().getScopesList());
    assertEquals("ACTIVE", registered.getApp().getStatus());

    ListAppsResponse list = dev.listApps(ListAppsRequest.newBuilder().build()).await().atMost(TIMEOUT);
    assertTrue(list.getAppsList().stream().anyMatch(a -> a.getId().equals(appId)));

    RotateSecretResponse rotated = dev.rotateSecret(RotateSecretRequest.newBuilder().setAppId(appId).build())
        .await().atMost(TIMEOUT);
    assertFalse(rotated.getClientSecret().isBlank());
    assertNotEquals(registered.getClientSecret(), rotated.getClientSecret());
    assertEquals(registered.getApp().getClientId(), rotated.getClientId());

    AppSummary updated = dev.setScopes(SetScopesRequest.newBuilder().setAppId(appId).addScopes("identity:read").build())
        .await().atMost(TIMEOUT);
    assertEquals(List.of("identity:read"), updated.getScopesList());
  }

  @Test
  void registerWithNoScopesYieldsEmptyScopeList() throws Throwable {
    DeveloperService dev = asVerifiedOwner();
    RegisterAppResponse registered = dev.registerApp(RegisterAppRequest.newBuilder().setName("Scopeless").build())
        .await().atMost(TIMEOUT);
    assertTrue(registered.getApp().getScopesList().isEmpty());
    assertEquals("Scopeless", registered.getApp().getName());
  }

  @Test
  void registerRejectsUnknownScope() throws Throwable {
    DeveloperService dev = asVerifiedOwner();
    assertEquals(Status.Code.INVALID_ARGUMENT, failureCode(dev.registerApp(
        RegisterAppRequest.newBuilder().setName("x").addScopes("bogus:scope").build())));
  }

  @Test
  void setScopesRejectsUnknownScope() throws Throwable {
    DeveloperService dev = asVerifiedOwner();
    String appId = dev.registerApp(RegisterAppRequest.newBuilder().setName("app").addScopes("posts:read").build())
        .await().atMost(TIMEOUT).getApp().getId();
    assertEquals(Status.Code.INVALID_ARGUMENT, failureCode(
        dev.setScopes(SetScopesRequest.newBuilder().setAppId(appId).addScopes("bogus:scope").build())));
  }

  @Test
  void setScopesUnknownAppIsNotFound() throws Throwable {
    DeveloperService dev = asVerifiedOwner();
    assertEquals(Status.Code.NOT_FOUND, failureCode(dev.setScopes(
        SetScopesRequest.newBuilder().setAppId(java.util.UUID.randomUUID().toString()).addScopes("posts:read").build())));
  }

  @Test
  void rotateUnknownAppIsNotFound() throws Throwable {
    DeveloperService dev = asVerifiedOwner();
    // Well-formed UUID id that the owner has no app for.
    String unknown = java.util.UUID.randomUUID().toString();
    assertEquals(Status.Code.NOT_FOUND,
        failureCode(dev.rotateSecret(RotateSecretRequest.newBuilder().setAppId(unknown).build())));
  }

  @Test
  void malformedAppIdIsNotFound() throws Throwable {
    DeveloperService dev = asVerifiedOwner();
    assertEquals(Status.Code.NOT_FOUND,
        failureCode(dev.rotateSecret(RotateSecretRequest.newBuilder().setAppId("not-base-36-!!!").build())));
  }

  @Test
  void nonVerifiedUserIsDenied() throws Throwable {
    // A plain USER-role session does not meet min_role ROLE_VERIFIED.
    String userSid = VertxContextSupport.subscribeAndAwait(() -> seeder.seedValidatedSession());
    assertEquals(Status.Code.PERMISSION_DENIED, failureCode(
        asOwner(userSid).registerApp(RegisterAppRequest.newBuilder().setName("x").build())));
  }
}
