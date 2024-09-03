package app.dissipate.utils;

import java.util.Random;

public class StringUtil {

  private static final Random random = new Random();

  public static String generateRandomString(int length) {
    String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder builder = new StringBuilder();
    while (length-- != 0) {
      int character = random.nextInt() * alphaNumericString.length();
      builder.append(alphaNumericString.charAt(character));
    }
    return builder.toString();
  }

  public static String generateRandomNumericString(int length) {
    String alphaNumericString = "0123456789";
    StringBuilder builder = new StringBuilder();
    while (length-- != 0) {
      int character = random.nextInt() * alphaNumericString.length();
      builder.append(alphaNumericString.charAt(character));
    }
    return builder.toString();
  }
}
