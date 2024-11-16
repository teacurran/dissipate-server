package app.dissipate.data.models;

import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;

import java.util.TimeZone;

@Entity
@Table(name = "country_timezones", indexes = {@Index(name = "idx_country_timezone_country", columnList = "country_id"), @Index(name = "idx_country_timezone", columnList = "country_id, timezone"),})
@NamedQuery(name = CountryTimezone.QUERY_FIND_BY_COUNTRY_TIMEZONE, query = """
  FROM CountryTimezone ct
  WHERE ct.country = :country
  AND ct.timezone = :timezone
  """)
public class CountryTimezone extends DefaultPanacheEntityWithTimestamps {
  public static final String ID_GENERATOR_KEY = "CountryTimezone";

  public static final String QUERY_FIND_BY_COUNTRY_TIMEZONE = "CountryTimezone.findByCountryTimezone";

  @ManyToOne
  @JoinColumn(name = "country_id")
  public Country country;

  public TimeZone timezone;

  public static Uni<CountryTimezone> findByCountryTimezone(Country country, TimeZone timezone) {
    return find("#" + CountryTimezone.QUERY_FIND_BY_COUNTRY_TIMEZONE, Parameters.with("country", country)
      .and("timezone", timezone)).firstResult();
  }
}
