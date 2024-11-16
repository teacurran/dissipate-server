package app.dissipate.utils;

import java.util.Random;

public class StringUtil {

  private StringUtil() {
    // class cannot be instantiated
  }

  public static String generateRandomString(int length) {
    Random random = new Random();
    String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder builder = new StringBuilder();
    while (length-- != 0) {
      int character = random.nextInt(alphaNumericString.length());
      builder.append(alphaNumericString.charAt(character));
    }
    return builder.toString();
  }

  public static String generateRandomNumericString(int length) {
    Random random = new Random();
    String alphaNumericString = "0123456789";
    StringBuilder builder = new StringBuilder();
    while (length-- != 0) {
      int character = random.nextInt(alphaNumericString.length());
      builder.append(alphaNumericString.charAt(character));
    }
    return builder.toString();
  }
}
