package app.dissipate.api.grpc;

import app.dissipate.data.models.Identity;
import app.dissipate.grpc.ChangeIdentityRequest;
import app.dissipate.grpc.ChangeIdentityResponse;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import app.dissipate.services.LocalizationService;
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
  CurrentIdentityAssociation identityAssociation;

  @WithSpan("ChangeIdentityMethod.change")
  public Uni<ChangeIdentityResponse> change(ChangeIdentityRequest request) {
    return identityAssociation.getDeferredIdentity().onItem().transformToUni(si -> {
      String subject = si.getPrincipal().getName();
      Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();

      return Identity.findById(request.getIid()).onItem().transformToUni(i -> {
        if (i == null) {
          return Uni.createFrom().failure(localizationService.getApiException(locale, Status.PERMISSION_DENIED, AUTH_EMAIL_INVALID));
        }

        // TODO: validate identity ownership via account linking once implemented
        return Uni.createFrom().item(ChangeIdentityResponse.newBuilder()
          .setIid(i.id)
          .setSid(subject)
          .setUsername(i.username)
          .setName(i.name).build());
      });
    });
  }
}
