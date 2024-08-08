package app.dissipate.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.time.ZoneOffset;

@ApplicationScoped
public class DateService {
  ZoneOffset zoneOffset = ZoneOffset.UTC;

  @Produces
  ZoneOffset getZoneOffset() {
    return zoneOffset;
  }
}
