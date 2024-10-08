package app.dissipate.data.location.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

@JsonRootName("country")
public class CountryJson {
  public int id;
  public String name;
  public String iso3;
  public String iso2;
  @JsonProperty("numeric_code")
  public String numericCode;
  @JsonProperty("phone_code")
  public String phoneCode;
  public String capital;
  public String currency;
  @JsonProperty("currency_name")
  public String currencyName;
  @JsonProperty("currency_symbol")
  public String currencySymbol;
  public String tld;
  @JsonProperty("native")
  public String nativeName;
  public String region;
  @JsonProperty("region_id")
  public String regionId;
  public String subregion;
  @JsonProperty("subregion_id")
  public String subregionId;
  public String nationality;
  public List<TimezoneJson> timezones;
  @JsonDeserialize(using = CountryTranslationDeserializer.class)
  public List<CountryTranslationJson> translations;
  public String latitude;
  public String longitude;
  public String emoji;
  @JsonProperty("emojiU")
  public String emojiUnicode;
  public List<StateJson> states;
}
