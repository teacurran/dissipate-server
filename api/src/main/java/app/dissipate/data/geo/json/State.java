package app.dissipate.data.geo.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class State {
  public int id;
  public String name;
  @JsonProperty("state_code")
  public String stateCode;
  public String latitude;
  public String longitude;
  public String type;
  public List<City> cities;
}
