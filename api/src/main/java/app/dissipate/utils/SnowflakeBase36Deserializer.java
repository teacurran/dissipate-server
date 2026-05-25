package app.dissipate.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Deserializes base-36 strings (or raw numeric ids) into {@code Long} Snowflake IDs.
 */
public class SnowflakeBase36Deserializer extends JsonDeserializer<Long> {

  @Override
  public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String s = p.getValueAsString();
    if (s == null || s.isEmpty()) {
      return null;
    }
    return Long.parseLong(s, 36);
  }
}
