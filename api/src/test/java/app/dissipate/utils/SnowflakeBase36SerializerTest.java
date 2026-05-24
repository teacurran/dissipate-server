package app.dissipate.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnowflakeBase36SerializerTest {

  static class Holder {
    @JsonSerialize(using = SnowflakeBase36Serializer.class)
    @JsonDeserialize(using = SnowflakeBase36Deserializer.class)
    public Long id;
  }

  @Test
  void roundTripsArbitrarySnowflakeValue() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    Holder h = new Holder();
    h.id = 1234567890123456789L; // ~63-bit positive

    String json = mapper.writeValueAsString(h);
    String expectedBase36 = Long.toString(h.id, 36);
    assertTrue(json.contains("\"" + expectedBase36 + "\""),
      "Serialized JSON should contain base-36 string id: " + json);

    Holder back = mapper.readValue(json, Holder.class);
    assertEquals(h.id, back.id);
  }

  @Test
  void roundTripsZero() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    Holder h = new Holder();
    h.id = 0L;
    String json = mapper.writeValueAsString(h);
    Holder back = mapper.readValue(json, Holder.class);
    assertEquals(0L, back.id);
  }

  @Test
  void serializesNullAsNull() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    Holder h = new Holder();
    h.id = null;
    String json = mapper.writeValueAsString(h);
    Holder back = mapper.readValue(json, Holder.class);
    assertNull(back.id);
  }
}
