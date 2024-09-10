package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "country_timezones")
public class CountryTimezone extends DefaultPanacheEntityWithTimestamps {

  public String country;

  @ManyToOne
  public TimeZone timezone;
}
