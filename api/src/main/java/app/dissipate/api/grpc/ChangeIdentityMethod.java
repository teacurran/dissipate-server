package app.dissipate.api.grpc;

import app.dissipate.auth.PrincipalResolver;
import app.dissipate.data.models.Identity;
import app.dissipate.grpc.v1.ChangeIdentityRequest;
import app.dissipate.grpc.v1.ChangeIdentityResponse;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import app.dissipate.services.LocalizationService;
import io.grpc.Status;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Locale;
import java.util.UUID;

import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_EMAIL_INVALID;

@ApplicationScoped
public class ChangeIdentityMethod {

  @Inject
  LocalizationService localizationService;

  @Inject
  PrincipalResolver principalResolver;

  @WithSpan("ChangeIdentityMethod.change")
  public Uni<ChangeIdentityResponse> change(ChangeIdentityRequest request) {
    return principalResolver.authorize().onItem().transformToUni(principal -> {
      String sid = principalResolver.session().id.toString();
      Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();

      final UUID iid;
      try {
        iid = UUID.fromString(request.getIid());
      } catch (IllegalArgumentException e) {
        return Uni.createFrom().failure(localizationService.getApiException(locale, Status.PERMISSION_DENIED, AUTH_EMAIL_INVALID));
      }
      return Identity.findById(iid).onItem().transformToUni(i -> {
        if (i == null) {
          return Uni.createFrom().failure(localizationService.getApiException(locale, Status.PERMISSION_DENIED, AUTH_EMAIL_INVALID));
        }

        // TODO: validate identity ownership via account linking once implemented
        return Uni.createFrom().item(ChangeIdentityResponse.newBuilder()
          .setIid(i.id.toString())
          .setSid(sid)
          .setUsername(i.username)
          .setName(i.name).build());
      });
    });
  }
}
