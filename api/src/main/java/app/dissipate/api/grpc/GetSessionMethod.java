package app.dissipate.api.grpc;

import app.dissipate.auth.PrincipalResolver;
import app.dissipate.data.models.Session;
import app.dissipate.grpc.v1.AccountStatus;
import app.dissipate.grpc.v1.GetSessionResponse;
import app.dissipate.grpc.v1.GetSessionRequest;
import com.google.protobuf.Timestamp;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;

@ApplicationScoped
public class GetSessionMethod {

  @Inject
  PrincipalResolver principalResolver;

  @WithSpan("GetSessionMethod.handler")
  public Uni<GetSessionResponse> handler(GetSessionRequest request) {
    // GetSession declares min_role ROLE_USER, so authorize() guarantees a resolved session.
    return principalResolver.authorize()
        .onItem().transform(principal -> build(principalResolver.session()));
  }

  private static GetSessionResponse build(Session session) {
    GetSessionResponse.Builder rb = GetSessionResponse.newBuilder()
        .setSid(session.id.toString())
        .setStatus(status(session));
    if (session.identity != null) {
      rb.setIid(Long.toString(session.identity.id, 36));
    }
    if (session.created != null) {
      rb.setCreated(toTimestamp(session.created));
    }
    if (session.updated != null) {
      rb.setLastSeen(toTimestamp(session.updated));
    }
    return rb.build();
  }

  private static AccountStatus status(Session session) {
    if (session.account == null || session.account.status == null) {
      return AccountStatus.ACCOUNT_STATUS_UNSPECIFIED;
    }
    return AccountStatus.valueOf("ACCOUNT_STATUS_" + session.account.status.name());
  }

  private static Timestamp toTimestamp(Instant instant) {
    return Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }
}
