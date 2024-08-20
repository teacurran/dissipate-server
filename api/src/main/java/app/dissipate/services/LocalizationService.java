package app.dissipate.services;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

@ApplicationScoped
public class LocalizationService {

  Map<Locale, ResourceBundle> bundles;

  public static final Locale DEFAULT_LOCALE = new Locale.Builder().setLanguage("en").setRegion("US").build();

  public static final Locale US_SPANISH = new Locale.Builder().setLanguage("es").setRegion("US").build();

  public LocalizationService() {
    bundles = Map.of(
      DEFAULT_LOCALE, ResourceBundle.getBundle("locales.i18n", DEFAULT_LOCALE),
      US_SPANISH, ResourceBundle.getBundle("locales.i18n", US_SPANISH)
    );
  }

  public ResourceBundle getBundle(Locale locale) {
    if (!bundles.containsKey(locale)) {
      return bundles.get(DEFAULT_LOCALE);
    }
    return bundles.get(locale);
  }
}