package app.dissipate.services;

import app.dissipate.exceptions.ApiException;
import io.grpc.Status;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;

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
    if (!bundles.containsKey(locale)) {
      return bundles.get(DEFAULT_LOCALE);
    }
    return bundles.get(locale);
  }

  public ApiException getApiException(Locale locale, Status status, String code) {
    ResourceBundle i18n = getBundle(locale);
    return new ApiException(status, code, i18n.getString(code));
  }
}
