package app.dissipate.services;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.location.json.CityJson;
import app.dissipate.data.location.json.CountryJson;
import app.dissipate.data.location.json.CountryTranslationJson;
import app.dissipate.data.location.json.StateJson;
import app.dissipate.data.location.json.TimezoneJson;
import app.dissipate.data.models.City;
import app.dissipate.data.models.Country;
import app.dissipate.data.models.CountryTimezone;
import app.dissipate.data.models.CountryTranslation;
import app.dissipate.data.models.State;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.util.zip.ZipFile.OPEN_READ;

@ApplicationScoped
public class EtlLocation {
  private static final Logger LOGGER = Logger.getLogger(EtlLocation.class);

  @Inject
  SnowflakeIdGenerator snowflakeIdGenerator;

  @Inject
  ObjectMapper mapper;

  @WithSpan
  public Uni<List<CountryJson>> parseLocationFile(InputStream file) {
    return Uni.createFrom().item(() -> {
      try {
        return mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, CountryJson.class));
      } catch (IOException e) {
        // Throw an unchecked exception to ensure the Uni fails
        throw new RuntimeException("Failed to parse JSON", e);
      }
    });
  }

  @WithSpan
  public Uni<Void> loadWorldLocations() {
    try {
      String zipFilePath = "db/seeds/countries-states-cities.json.zip";

      ZipFile zipFile = new ZipFile(new File(Thread.currentThread().getContextClassLoader().getResource(zipFilePath).toURI()), OPEN_READ);
      ZipEntry entry = zipFile.getEntry("countries-states-cities.json");
      InputStream is = zipFile.getInputStream(entry);

      return parseLocationFile(is).onItem().transformToUni(countryJsons -> {
        LOGGER.infov("Loading {0} countries", countryJsons.size());

        return Multi.createFrom().iterable(countryJsons).onItem().transformToUniAndConcatenate(countryJson -> {
          Country country = new Country();
          country.id = String.valueOf(countryJson.id);
          country.capital = countryJson.capital;
          country.currency = countryJson.currency;
          country.currencyName = countryJson.currencyName;
          country.currencySymbol = countryJson.currencySymbol;
          country.emoji = countryJson.emoji;
          country.emojiU = countryJson.emojiUnicode;
          country.iso2 = countryJson.iso2;
          country.iso3 = countryJson.iso3;
          country.name = countryJson.name;
          country.nationality = countryJson.nationality;
          country.nativeName = countryJson.nativeName;
          country.phoneCode = countryJson.phoneCode;
          country.region = countryJson.region;
          country.regionId = countryJson.regionId;
          country.subregion = countryJson.subregion;
          country.subregionId = countryJson.subregionId;

          // Geometry not working
          // country.location = point(WGS84, g(Double.parseDouble(countryJson.longitude), Double.parseDouble(countryJson.latitude)));

          return Panache.getSession().onItem().transformToUni(session -> session.merge(country)
            .onItem().transformToUni(c -> processStates(session, countryJson.states, c)
              .replaceWith(processTranslations(session, countryJson.translations, c)
                .replaceWith(processTimezones(session, countryJson.timezones, c)))));
        }).collect().asList().replaceWith(Uni.createFrom().nullItem());
      });
    } catch (IOException | URISyntaxException e) {
      return Uni.createFrom().failure(e);
    }
  }

  @WithSpan
  public Uni<Void> processStates(Mutiny.Session session, List<StateJson> states, Country country) {
    return Multi.createFrom().iterable(states)
      .onItem().transformToUniAndConcatenate(stateJson -> {
        State state = new State();
        state.id = String.valueOf(stateJson.id);
        state.name = stateJson.name;
        state.country = country;
        return session.merge(state)
          .onItem().transformToUni(s -> processCities(session, stateJson.cities, s, country));
      }).collect().asList().replaceWith(Uni.createFrom().nullItem());
  }

  @WithSpan
  public Uni<Void> processCities(Mutiny.Session session, List<CityJson> cities, State state, Country country) {
    return Multi.createFrom().iterable(cities)
      .onItem().transformToUniAndConcatenate(cityJson -> {
        City city = new City();
        city.id = String.valueOf(cityJson.id);
        city.state = state;
        city.country = country;
        city.name = cityJson.name;
        return session.merge(city);
      }).collect().asList().replaceWith(Uni.createFrom().nullItem());
  }

  @WithSpan
  public Uni<Void> processTranslations(Mutiny.Session session, List<CountryTranslationJson> translations, Country country) {
    return Multi.createFrom().iterable(translations)
      .onItem().transformToUniAndConcatenate(translationJson -> {
        CountryTranslation countryTranslation = new CountryTranslation();
        countryTranslation.id = country.id + "-" + translationJson.language;
        countryTranslation.country = country;
        countryTranslation.locale = Locale.forLanguageTag(translationJson.language);
        countryTranslation.name = translationJson.translation;
        return session.merge(countryTranslation);
      }).collect().asList().replaceWith(Uni.createFrom().nullItem());
  }

  @WithSpan
  public Uni<Void> processTimezones(Mutiny.Session session, List<TimezoneJson> timezones, Country country) {
    return Multi.createFrom().iterable(timezones)
      .onItem().transformToUniAndConcatenate(timezoneJson -> {
        TimeZone tz = TimeZone.getTimeZone(timezoneJson.zoneName);
        return CountryTimezone.findByCountryTimezone(country, tz).onItem().transformToUni(ct -> {
          if (ct == null) {
            CountryTimezone countryTimezone = new CountryTimezone();
            countryTimezone.id = snowflakeIdGenerator.generate(CountryTimezone.ID_GENERATOR_KEY);
            countryTimezone.country = country;
            countryTimezone.timezone = tz;
            return session.merge(countryTimezone);
          }
          return Uni.createFrom().nullItem();
        });
      }).collect().asList().replaceWith(Uni.createFrom().nullItem());
  }
}
