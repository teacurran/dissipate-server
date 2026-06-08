package app.dissipate.api.rest.i18n;

import app.dissipate.services.LocalizationService;
import jakarta.enterprise.context.RequestScoped;

import java.util.Locale;

/**
 * Request-scoped holder for the locale resolved from the {@code Accept-Language} header by
 * {@link RestLocaleFilter}. The REST equivalent of {@code GrpcLocaleInterceptor.LOCALE_CONTEXT_KEY}.
 * Defaults to {@link LocalizationService#DEFAULT_LOCALE} until the filter runs.
 */
@RequestScoped
public class RequestLocale {

  private Locale locale = LocalizationService.DEFAULT_LOCALE;

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }
}
