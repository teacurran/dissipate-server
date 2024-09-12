package app.dissipate.data.location.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TimezoneJson {
  public String zoneName;
  public int gmtOffset;
  @JsonProperty("gmtOffsetName")
  public String gmtOffsetName;
  public String abbreviation;
  public String tzName;
}
