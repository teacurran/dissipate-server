package app.dissipate.utils;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * JAX-RS provider that converts base-36 path/query parameters annotated with
 * {@link SnowflakeId} to {@link Long} Snowflake IDs.
 *
 * <p>This deliberately opts in by annotation rather than catching every
 * {@code Long} parameter: not all numeric REST parameters in the app are
 * Snowflake IDs (e.g. paging offsets, autoincrement {@code Server.id}, etc.).
 */
@Provider
public class SnowflakeIdParamConverterProvider implements ParamConverterProvider {

  @Override
  @SuppressWarnings("unchecked")
  public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
    if (rawType != Long.class && rawType != long.class) {
      return null;
    }
    boolean hasMarker = false;
    if (annotations != null) {
      for (Annotation a : annotations) {
        if (a.annotationType() == SnowflakeId.class) {
          hasMarker = true;
          break;
        }
      }
    }
    if (!hasMarker) {
      return null;
    }
    return (ParamConverter<T>) BASE36_CONVERTER;
  }

  private static final ParamConverter<Long> BASE36_CONVERTER = new ParamConverter<>() {
    @Override
    public Long fromString(String value) {
      if (value == null || value.isEmpty()) {
        return null;
      }
      return Long.parseLong(value, 36);
    }

    @Override
    public String toString(Long value) {
      return value == null ? null : Long.toString(value, 36);
    }
  };
}
