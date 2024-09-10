package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.geolatte.geom.Point;

import java.util.List;

@Entity
@Table(name = "countries")
public class Country extends DefaultPanacheEntityWithTimestamps {

  public String name;
  public String iso3;
  public String iso2;
  public String numericCode;
  public String phoneCode;
  public String capital;
  public String currency;
  public String currencyName;
  public String currencySymbol;
  public String tld;
  public String nativeName;
  public String region;
  public String regionId;
  public String subregion;
  public String subregionId;
  public String nationality;
  public Point location;
  public String emoji;
  public String emojiU;

  @OneToMany(mappedBy = "country")
  List<CountryTranslation> names;

  @OneToMany(mappedBy = "country")
  List<State> states;
}
