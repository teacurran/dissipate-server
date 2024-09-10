package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "time_zones")
public class TimeZone extends DefaultPanacheEntityWithTimestamps {
  public String zoneName;
  public Integer gmtOffset;
  public String gmtOffsetName;
  public String abbreviation;
  public String tzName;
}
