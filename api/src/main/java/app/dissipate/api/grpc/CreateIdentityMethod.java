package app.dissipate.api.grpc;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.models.Identity;
import app.dissipate.data.models.Session;
import app.dissipate.grpc.CreateIdentityRequest;
import app.dissipate.grpc.CreateIdentityResponse;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import app.dissipate.services.LocalizationService;
import app.dissipate.utils.EncryptionUtil;
import io.grpc.Status;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Locale;

import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_EMAIL_INVALID;

@ApplicationScoped
public class CreateIdentityMethod {

  @Inject
  LocalizationService localizationService;

  @Inject
  SnowflakeIdGenerator snowflakeIdGenerator;

  @Inject
  EncryptionUtil encryptionUtil;

  @Inject
  CurrentIdentityAssociation identityAssociation;

  @WithSpan("CreateIdentityMethod.create")
  public Uni<CreateIdentityResponse> create(CreateIdentityRequest request) {
    return identityAssociation.getDeferredIdentity().onItem().transformToUni(si -> {
      Session session = si.getAttribute("session");

      if (session == null || session.account == null) {
        Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();
        return Uni.createFrom().failure(localizationService.getApiException(locale, Status.PERMISSION_DENIED, AUTH_EMAIL_INVALID));
      }

      Identity identity = new Identity();
      identity.id = snowflakeIdGenerator.generate(Identity.ID_GENERATOR_KEY);
      identity.account = session.account;
      identity.username = request.getUsername();
      identity.name = request.getName();
      return identity.persistAndFlush(encryptionUtil)
        .onItem().transformToUni(i -> {
          session.identity = i;
          return session.persistAndFlush().onItem().transform(s-> CreateIdentityResponse.newBuilder()
            .setIid(i.id)
            .setSid(s.id.toString())
            .setUsername(i.username)
            .setName(i.name).build());
        });
    });
  }


}
