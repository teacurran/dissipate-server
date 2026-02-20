package app.dissipate.api.grpc;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.models.Identity;
import app.dissipate.grpc.CreateIdentityRequest;
import app.dissipate.grpc.CreateIdentityResponse;
import app.dissipate.utils.EncryptionUtil;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.security.identity.CurrentIdentityAssociation;
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
  CurrentIdentityAssociation identityAssociation;

  @WithSpan("CreateIdentityMethod.create")
  public Uni<CreateIdentityResponse> create(CreateIdentityRequest request) {
    return identityAssociation.getDeferredIdentity().onItem().transformToUni(si -> {
      String subject = si.getPrincipal().getName();

      Identity identity = new Identity();
      identity.id = snowflakeIdGenerator.generate(Identity.ID_GENERATOR_KEY);
      // account linking will be implemented later — for now identity is standalone
      identity.username = request.getUsername();
      identity.name = request.getName();
      return identity.persistAndFlush(encryptionUtil)
        .onItem().transform(i -> CreateIdentityResponse.newBuilder()
          .setIid(i.id)
          .setSid(subject)
          .setUsername(i.username)
          .setName(i.name).build());
    });
  }
}
