package app.dissipate.data.geo.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Timezone {
  public String zoneName;
  public int gmtOffset;
  @JsonProperty("gmtOffsetName")
  public String gmtOffsetName;
  public String abbreviation;
  public String tzName;
}
