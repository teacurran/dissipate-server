package app.dissipate.api.grpc;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.models.Identity;
import app.dissipate.data.models.Session;
import app.dissipate.grpc.ChangeIdentityRequest;
import app.dissipate.grpc.ChangeIdentityResponse;
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
public class ChangeIdentityMethod {

  @Inject
  LocalizationService localizationService;

  @Inject
  SnowflakeIdGenerator snowflakeIdGenerator;

  @Inject
  EncryptionUtil encryptionUtil;

  @Inject
  CurrentIdentityAssociation identityAssociation;

  @WithSpan("CreateIdentityMethod.create")
  public Uni<CreateIdentityResponse> change(ChangeIdentityRequest request) {
    return identityAssociation.getDeferredIdentity().onItem().transformToUni(si -> {
      Session session = si.getAttribute("session");

      if (session == null || session.account == null) {
        Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();
        return Uni.createFrom().failure(localizationService.getApiException(locale, Status.PERMISSION_DENIED, AUTH_EMAIL_INVALID));
      }

      Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();

      Identity.findById(request.getIid()).onItem().transform(i -> {
        if (i == null) {
          return Uni.createFrom().failure(localizationService.getApiException(locale, Status.PERMISSION_DENIED, AUTH_EMAIL_INVALID));
        }
        if (i.account != session.account) {
          return Uni.createFrom().failure(localizationService.getApiException(locale, Status.PERMISSION_DENIED, AUTH_EMAIL_INVALID));
        }

        session.identity = i;
        return session.persistAndFlush().onItem().transform(s-> ChangeIdentityResponse.newBuilder()
          .setIid(i.id)
          .setSid(s.id.toString())
          .setUsername(i.username)
          .setName(i.name).build());
      });

    });
  }


}
