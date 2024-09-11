package app.dissipate.data.geo.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class Country {
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
  public List<Timezone> timezones;
  public Map<String, String> translations;
  public String latitude;
  public String longitude;
  public String emoji;
  @JsonProperty("emojiU")
  public String emojiUnicode;
  public List<State> states;
}
