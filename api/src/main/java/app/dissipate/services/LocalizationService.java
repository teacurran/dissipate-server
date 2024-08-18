package app.dissipate.services;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Locale;
import java.util.ResourceBundle;

@ApplicationScoped
public class LocalizationService {

  public ResourceBundle getBundle(Locale locale) {
    return ResourceBundle.getBundle("i18n.messages", locale);
  }
}
