// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: dissipate.proto
// Protobuf Java Version: 4.27.3

package app.dissipate.grpc;

public interface RegisterRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:RegisterRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional string locale = 1 [json_name = "locale"];</code>
   * @return Whether the locale field is set.
   */
  boolean hasLocale();
  /**
   * <code>optional string locale = 1 [json_name = "locale"];</code>
   * @return The locale.
   */
  java.lang.String getLocale();
  /**
   * <code>optional string locale = 1 [json_name = "locale"];</code>
   * @return The bytes for locale.
   */
  com.google.protobuf.ByteString
      getLocaleBytes();

  /**
   * <code>optional string email = 2 [json_name = "email"];</code>
   * @return Whether the email field is set.
   */
  boolean hasEmail();
  /**
   * <code>optional string email = 2 [json_name = "email"];</code>
   * @return The email.
   */
  java.lang.String getEmail();
  /**
   * <code>optional string email = 2 [json_name = "email"];</code>
   * @return The bytes for email.
   */
  com.google.protobuf.ByteString
      getEmailBytes();

  /**
   * <code>optional string phone_number = 3 [json_name = "phoneNumber"];</code>
   * @return Whether the phoneNumber field is set.
   */
  boolean hasPhoneNumber();
  /**
   * <code>optional string phone_number = 3 [json_name = "phoneNumber"];</code>
   * @return The phoneNumber.
   */
  java.lang.String getPhoneNumber();
  /**
   * <code>optional string phone_number = 3 [json_name = "phoneNumber"];</code>
   * @return The bytes for phoneNumber.
   */
  com.google.protobuf.ByteString
      getPhoneNumberBytes();
}
