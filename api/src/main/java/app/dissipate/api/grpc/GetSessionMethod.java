package app.dissipate.api.grpc;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.grpc.GetSessionRequest;
import app.dissipate.grpc.GetSessionResponse;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import app.dissipate.services.DelayedJobService;
import app.dissipate.services.LocalizationService;
import app.dissipate.utils.EncryptionUtil;
import io.grpc.Status;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Locale;

import static app.dissipate.api.grpc.GrpcErrorCodes.AUTH_TOKEN_INVALID;

@ApplicationScoped
public class GetSessionMethod {

  private static final Logger LOGGER = Logger.getLogger(GetSessionMethod.class);

  @Inject
  DelayedJobService delayedJobService;

  @Inject
  LocalizationService localizationService;

  @Inject
  SnowflakeIdGenerator snowflakeIdGenerator;

  @Inject
  EncryptionUtil encryptionUtil;

  @Inject
  CurrentIdentityAssociation identity;

  @WithSpan("GetSessionMethod.handler")
  @WithSession
  public Uni<GetSessionResponse> handler(GetSessionRequest request) {
    return identity.getDeferredIdentity().onItem().transformToUni(si -> {
      LOGGER.info("GetSessionMethod.handler: " + si.getPrincipal());
      if (si == null || si.getPrincipal() == null) {
        Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();
        return Uni.createFrom().failure(localizationService.getApiException(locale, Status.NOT_FOUND, AUTH_TOKEN_INVALID));
      }
      return Uni.createFrom().item(GetSessionResponse.newBuilder()
        .setSid(si.getPrincipal().getName()).build());
    });
  }
}
