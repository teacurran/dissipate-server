package app.dissipate.api.grpc;

import app.dissipate.auth.PrincipalResolver;
import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.models.Identity;
import app.dissipate.grpc.v1.CreateIdentityRequest;
import app.dissipate.grpc.v1.CreateIdentityResponse;
import app.dissipate.utils.EncryptionUtil;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CreateIdentityMethod {

  @Inject
  SnowflakeIdGenerator snowflakeIdGenerator;

  @Inject
  EncryptionUtil encryptionUtil;

  @Inject
  PrincipalResolver principalResolver;

  @WithSpan("CreateIdentityMethod.create")
  public Uni<CreateIdentityResponse> create(CreateIdentityRequest request) {
    return principalResolver.authorize().onItem().transformToUni(principal -> {
      String sid = principalResolver.session().id.toString();

      Identity identity = new Identity();
      identity.id = snowflakeIdGenerator.generate(Identity.ID_GENERATOR_KEY);
      // account linking will be implemented later — for now identity is standalone
      identity.username = request.getUsername();
      identity.name = request.getName();
      return identity.persistAndFlush(encryptionUtil)
        .onItem().transform(i -> CreateIdentityResponse.newBuilder()
          .setIid(Long.toString(i.id, 36))
          .setSid(sid)
          .setUsername(i.username)
          .setName(i.name).build());
    });
  }
}
