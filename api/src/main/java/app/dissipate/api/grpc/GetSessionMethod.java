package app.dissipate.api.grpc;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.grpc.GetSessionRequest;
import app.dissipate.grpc.GetSessionResponse;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import app.dissipate.services.DelayedJobService;
import app.dissipate.services.LocalizationService;
import app.dissipate.utils.EncryptionUtil;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Locale;

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

  @WithSpan("RegisterMethod.register")
  public Uni<GetSessionResponse> handler(GetSessionRequest request) {
    Span otel = Span.current();
    Locale locale = GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY.get();

  }
}
