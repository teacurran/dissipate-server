package app.dissipate.api.rest.i18n;

import app.dissipate.services.LocalizationService;
import io.opentelemetry.api.trace.Span;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
import java.util.Locale;

/**
 * Resolves the request locale from {@code Accept-Language} and stashes it in {@link RequestLocale}.
 * Mirrors {@code GrpcLocaleInterceptor} for the HTTP layer. Runs before authentication so auth
 * errors are localized correctly. Falls back to {@link LocalizationService#DEFAULT_LOCALE}.
 */
@Provider
@Priority(Priorities.AUTHENTICATION - 100)
public class RestLocaleFilter implements ContainerRequestFilter {

  @Inject
  RequestLocale requestLocale;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    Locale resolved = resolve(requestContext.getAcceptableLanguages());
    requestLocale.setLocale(resolved);
    Span.current().setAttribute("locale", resolved.toLanguageTag());
  }

  /**
   * Pick the first acceptable language (already sorted by q-value) whose language tag maps to a
   * supported bundle. Matches by language so {@code es-MX} still resolves to {@code es_US}.
   */
  private Locale resolve(List<Locale> acceptable) {
    if (acceptable != null) {
      for (Locale candidate : acceptable) {
        if (candidate == null || candidate.getLanguage().isEmpty()) {
          continue;
        }
        if (LocalizationService.US_SPANISH.getLanguage().equals(candidate.getLanguage())) {
          return LocalizationService.US_SPANISH;
        }
        if (LocalizationService.DEFAULT_LOCALE.getLanguage().equals(candidate.getLanguage())) {
          return LocalizationService.DEFAULT_LOCALE;
        }
      }
    }
    return LocalizationService.DEFAULT_LOCALE;
  }
}
