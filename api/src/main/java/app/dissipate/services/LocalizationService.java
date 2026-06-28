package app.dissipate.services;

import app.dissipate.exceptions.ApiException;
import io.grpc.Status;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

@ApplicationScoped
public class LocalizationService {

  Map<Locale, ResourceBundle> bundles;

  public static final Locale DEFAULT_LOCALE = new Locale.Builder().setLanguage("en").setRegion("US").build();

  public static final Locale US_SPANISH = new Locale.Builder().setLanguage("es").setRegion("US").build();

  @Startup
  void init() {
    bundles = Map.of(
      DEFAULT_LOCALE, ResourceBundle.getBundle("locales.i18n", DEFAULT_LOCALE),
      US_SPANISH, ResourceBundle.getBundle("locales.i18n", US_SPANISH)
    );
  }

  public ResourceBundle getBundle(Locale locale) {
    // without this, compile with native profile fails
    if (bundles == null) {
      init();
    }
    // `bundles` is an immutable Map.of(...), whose containsKey(null) throws NPE — so null-check
    // first. A null locale is common: it is read from the gRPC context inside a @WithSession
    // reactive continuation, where that context is no longer current.
    if (locale == null || !bundles.containsKey(locale)) {
      return bundles.get(DEFAULT_LOCALE);
    }
    return bundles.get(locale);
  }

  public ApiException getApiException(Locale locale, Status status, String code) {
    ResourceBundle i18n = getBundle(locale);
    return new ApiException(status, code, i18n.getString(code));
  }

  /**
   * Resolve a localized message by key for the given locale, formatting any {@link MessageFormat}
   * placeholders with {@code args}. Used by the REST layer (gRPC uses {@link #getApiException}).
   */
  public String getMessage(Locale locale, String key, Object... args) {
    String pattern = getBundle(locale).getString(key);
    if (args == null || args.length == 0) {
      return pattern;
    }
    return new MessageFormat(pattern, locale).format(args);
  }
}
