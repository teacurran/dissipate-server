package app.dissipate.data.location.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class StateJson {
  public int id;
  public String name;
  @JsonProperty("state_code")
  public String stateCode;
  public String latitude;
  public String longitude;
  public String type;
  public List<CityJson> cities;
}
