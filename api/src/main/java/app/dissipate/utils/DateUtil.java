package app.dissipate.utils;

import java.text.SimpleDateFormat;

public class DateUtil {
  public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
  public static final String OTEL_DATE_TIME_FORMAT = DEFAULT_DATE_TIME_FORMAT;

  public static final SimpleDateFormat OTEL_DATE_TIME_FORMATTER = new SimpleDateFormat(OTEL_DATE_TIME_FORMAT);

  private DateUtil() {
    // class cannot be instantiated
  }
}
