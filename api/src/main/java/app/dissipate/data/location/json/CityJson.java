package app.dissipate.data.location.json;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("city")
public class CityJson {
  public int id;
  public String name;
  public String latitude;
  public String longitude;
}
