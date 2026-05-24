package app.dissipate.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Serializes {@code Long} Snowflake IDs as base-36 strings.
 *
 * <p>Snowflake values routinely exceed 2^53 and therefore cannot survive a round
 * trip through a JavaScript {@code number}. Emitting them as strings preserves
 * precision while base-36 keeps the wire form compact.
 */
public class SnowflakeBase36Serializer extends JsonSerializer<Long> {

  @Override
  public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    if (value == null) {
      gen.writeNull();
    } else {
      gen.writeString(Long.toString(value, 36));
    }
  }
}
