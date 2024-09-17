package app.dissipate.data.location.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CountryTranslationDeserializer extends JsonDeserializer<List<CountryTranslationJson>> {
  @Override
  public List<CountryTranslationJson> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    List<CountryTranslationJson> translations = new ArrayList<>();
    JsonNode node = p.getCodec().readTree(p);
    Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> field = fields.next();
      CountryTranslationJson translation = new CountryTranslationJson();
      translation.language = field.getKey();
      translation.translation = field.getValue().asText();
      translations.add(translation);
    }
    return translations;
  }
}
