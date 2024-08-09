package app.dissipate.utils;

public class StringUtil {
  public static String generateRandomString(int length) {
    String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder builder = new StringBuilder();
    while (length-- != 0) {
      int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
      builder.append(ALPHA_NUMERIC_STRING.charAt(character));
    }
    return builder.toString();
  }

  public static String generateRandomNumericString(int length) {
    String ALPHA_NUMERIC_STRING = "0123456789";
    StringBuilder builder = new StringBuilder();
    while (length-- != 0) {
      int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
      builder.append(ALPHA_NUMERIC_STRING.charAt(character));
    }
    return builder.toString();
  }
}
