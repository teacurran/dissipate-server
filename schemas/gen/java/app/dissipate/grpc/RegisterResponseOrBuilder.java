// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: dissipate.proto
// Protobuf Java Version: 4.27.3

package app.dissipate.grpc;

public interface RegisterResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:RegisterResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.RegisterResponseResult result = 1 [json_name = "result"];</code>
   * @return The enum numeric value on the wire for result.
   */
  int getResultValue();
  /**
   * <code>.RegisterResponseResult result = 1 [json_name = "result"];</code>
   * @return The result.
   */
  app.dissipate.grpc.RegisterResponseResult getResult();

  /**
   * <code>optional string sid = 2 [json_name = "sid"];</code>
   * @return Whether the sid field is set.
   */
  boolean hasSid();
  /**
   * <code>optional string sid = 2 [json_name = "sid"];</code>
   * @return The sid.
   */
  java.lang.String getSid();
  /**
   * <code>optional string sid = 2 [json_name = "sid"];</code>
   * @return The bytes for sid.
   */
  com.google.protobuf.ByteString
      getSidBytes();

  /**
   * <code>optional .ApiError error = 3 [json_name = "error"];</code>
   * @return Whether the error field is set.
   */
  boolean hasError();
  /**
   * <code>optional .ApiError error = 3 [json_name = "error"];</code>
   * @return The error.
   */
  app.dissipate.grpc.ApiError getError();
  /**
   * <code>optional .ApiError error = 3 [json_name = "error"];</code>
   */
  app.dissipate.grpc.ApiErrorOrBuilder getErrorOrBuilder();
}
