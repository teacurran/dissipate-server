package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Locale;

@Entity
@Table(name = "country_translations")
public class CountryTranslation extends DefaultPanacheEntityWithTimestamps {

  @ManyToOne
  public Country country;

  public Locale locale;

  public String name;

}
