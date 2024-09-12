package app.dissipate.services;

import app.dissipate.data.jpa.SnowflakeIdGenerator;
import app.dissipate.data.location.json.CountryJson;
import app.dissipate.data.models.Country;
import app.dissipate.data.models.State;
import app.dissipate.utils.EncryptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.core.runtime.context.VertxContextSafetyToggle;
import io.smallrye.common.vertx.VertxContext;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.util.zip.ZipFile.OPEN_READ;

@ApplicationScoped
public class WorldLocationLoader {
  private static final Logger LOGGER = Logger.getLogger(WorldLocationLoader.class);

  @Inject
  SnowflakeIdGenerator snowflakeIdGenerator;

  @Inject
  ObjectMapper mapper;

  @Inject
  Tracer tracer;

  @Inject
  EncryptionUtil encryptionUtil;

  @WithSpan("ConfigFileLoader.onStart")
  public void onStart(@Observes StartupEvent event, Vertx vertx, Mutiny.SessionFactory factory) {
    // Create a new Vertx context for Hibernate Reactive
    io.vertx.core.Context context = VertxContext.getOrCreateDuplicatedContext(vertx);
    // Mark the context as safe
    VertxContextSafetyToggle.setContextSafe(context, true);
    // Run the logic on the created context
    context.runOnContext(v -> handleStart(factory));
  }

  @WithSpan
  public void handleStart(Mutiny.SessionFactory factory) {
    // Start a new transaction
    factory.withTransaction(session -> loadConfig().onItem().transform(config -> {
        LOGGER.info("Config loaded: " + config);
        return config;
      })).onFailure().invoke(t -> {
        LOGGER.error("Failed to load config", t);
      })
      // Subscribe to the Uni to trigger the action
      .subscribe().with(v -> {
      });
  }

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
  public Uni<Void> loadConfig() {

    try {
      String zipFilePath = "db/seeds/countries-states-cities.json.zip";

      ZipFile zipFile = new ZipFile(new File(Thread.currentThread().getContextClassLoader().getResource(zipFilePath).toURI()), OPEN_READ);
      ZipEntry entry = zipFile.getEntry("countries-states-cities.json");
      InputStream is = zipFile.getInputStream(entry);

      return parseLocationFile(is).onItem().transformToUni(config -> {
        LOGGER.info("Config loaded: " + config);

        return Multi.createFrom().iterable(config).onItem().transformToUniAndConcatenate(countryJson -> {
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
            .onItem().transformToUni(c -> {
              return Multi.createFrom().iterable(countryJson.states).onItem().transformToUniAndConcatenate(stateJson -> {
                State state = new State();
                state.id = String.valueOf(stateJson.id);
                state.name = stateJson.name;
                state.country = c;
                return session.merge(state);
              }).collect().asList().replaceWith(Uni.createFrom().nullItem());
            }));
        }).collect().asList().replaceWith(Uni.createFrom().nullItem());
      });
    } catch (IOException | URISyntaxException e) {
      return Uni.createFrom().failure(e);
    }
  }
}
