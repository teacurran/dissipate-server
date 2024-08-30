package app.dissipate.interceptors;

import app.dissipate.data.jpa.converters.LocaleConverter;
import app.dissipate.services.LocalizationService;
import io.grpc.*;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Locale;

@ApplicationScoped
public class GrpcLocaleInterceptor implements ServerInterceptor {

  public static final String LOCALE_KEY_NAME = "locale";
  public static final Metadata.Key<String> LOCALE_HEADER_KEY = Metadata.Key.of(LOCALE_KEY_NAME, Metadata.ASCII_STRING_MARSHALLER);

  public static final Context.Key<Locale> LOCALE_CONTEXT_KEY = Context.key(LOCALE_KEY_NAME);

  @Inject
  LocalizationService localizationService;

  @Override
  @WithSpan("GrpcLocaleInterceptor")
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {
    Span otel = Span.current();

    String localeString = metadata.get(LOCALE_HEADER_KEY);
    Locale locale = LocaleConverter.fromValue(localeString);
    otel.setAttribute(LOCALE_KEY_NAME, locale.toLanguageTag());

    Context context = Context.current().withValue(LOCALE_CONTEXT_KEY, locale);

    return Contexts.interceptCall(context, serverCall, metadata, next);
  }

}
