// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: dissipate.proto
// Protobuf Java Version: 4.27.3

package app.dissipate.grpc;

/**
 * Protobuf type {@code ValidateSessionRequest}
 */
public final class ValidateSessionRequest extends
    com.google.protobuf.GeneratedMessage implements
    // @@protoc_insertion_point(message_implements:ValidateSessionRequest)
    ValidateSessionRequestOrBuilder {
private static final long serialVersionUID = 0L;
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 27,
      /* patch= */ 3,
      /* suffix= */ "",
      ValidateSessionRequest.class.getName());
  }
  // Use ValidateSessionRequest.newBuilder() to construct.
  private ValidateSessionRequest(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
    super(builder);
  }
  private ValidateSessionRequest() {
    sid_ = "";
    otp_ = "";
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return app.dissipate.grpc.DissipateProto.internal_static_ValidateSessionRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return app.dissipate.grpc.DissipateProto.internal_static_ValidateSessionRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            app.dissipate.grpc.ValidateSessionRequest.class, app.dissipate.grpc.ValidateSessionRequest.Builder.class);
  }

  public static final int SID_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object sid_ = "";
  /**
   * <code>string sid = 1 [json_name = "sid", (.buf.validate.field) = { ... }</code>
   * @return The sid.
   */
  @java.lang.Override
  public java.lang.String getSid() {
    java.lang.Object ref = sid_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      sid_ = s;
      return s;
    }
  }
  /**
   * <code>string sid = 1 [json_name = "sid", (.buf.validate.field) = { ... }</code>
   * @return The bytes for sid.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getSidBytes() {
    java.lang.Object ref = sid_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      sid_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int OTP_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private volatile java.lang.Object otp_ = "";
  /**
   * <code>string otp = 2 [json_name = "otp", (.buf.validate.field) = { ... }</code>
   * @return The otp.
   */
  @java.lang.Override
  public java.lang.String getOtp() {
    java.lang.Object ref = otp_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      otp_ = s;
      return s;
    }
  }
  /**
   * <code>string otp = 2 [json_name = "otp", (.buf.validate.field) = { ... }</code>
   * @return The bytes for otp.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getOtpBytes() {
    java.lang.Object ref = otp_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      otp_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (!com.google.protobuf.GeneratedMessage.isStringEmpty(sid_)) {
      com.google.protobuf.GeneratedMessage.writeString(output, 1, sid_);
    }
    if (!com.google.protobuf.GeneratedMessage.isStringEmpty(otp_)) {
      com.google.protobuf.GeneratedMessage.writeString(output, 2, otp_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessage.isStringEmpty(sid_)) {
      size += com.google.protobuf.GeneratedMessage.computeStringSize(1, sid_);
    }
    if (!com.google.protobuf.GeneratedMessage.isStringEmpty(otp_)) {
      size += com.google.protobuf.GeneratedMessage.computeStringSize(2, otp_);
    }
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof app.dissipate.grpc.ValidateSessionRequest)) {
      return super.equals(obj);
    }
    app.dissipate.grpc.ValidateSessionRequest other = (app.dissipate.grpc.ValidateSessionRequest) obj;

    if (!getSid()
        .equals(other.getSid())) return false;
    if (!getOtp()
        .equals(other.getOtp())) return false;
    if (!getUnknownFields().equals(other.getUnknownFields())) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + SID_FIELD_NUMBER;
    hash = (53 * hash) + getSid().hashCode();
    hash = (37 * hash) + OTP_FIELD_NUMBER;
    hash = (53 * hash) + getOtp().hashCode();
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static app.dissipate.grpc.ValidateSessionRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static app.dissipate.grpc.ValidateSessionRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static app.dissipate.grpc.ValidateSessionRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static app.dissipate.grpc.ValidateSessionRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static app.dissipate.grpc.ValidateSessionRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static app.dissipate.grpc.ValidateSessionRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static app.dissipate.grpc.ValidateSessionRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static app.dissipate.grpc.ValidateSessionRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static app.dissipate.grpc.ValidateSessionRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static app.dissipate.grpc.ValidateSessionRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static app.dissipate.grpc.ValidateSessionRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input);
  }
  public static app.dissipate.grpc.ValidateSessionRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessage
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(app.dissipate.grpc.ValidateSessionRequest prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessage.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code ValidateSessionRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessage.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:ValidateSessionRequest)
      app.dissipate.grpc.ValidateSessionRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return app.dissipate.grpc.DissipateProto.internal_static_ValidateSessionRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return app.dissipate.grpc.DissipateProto.internal_static_ValidateSessionRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              app.dissipate.grpc.ValidateSessionRequest.class, app.dissipate.grpc.ValidateSessionRequest.Builder.class);
    }

    // Construct using app.dissipate.grpc.ValidateSessionRequest.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      sid_ = "";
      otp_ = "";
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return app.dissipate.grpc.DissipateProto.internal_static_ValidateSessionRequest_descriptor;
    }

    @java.lang.Override
    public app.dissipate.grpc.ValidateSessionRequest getDefaultInstanceForType() {
      return app.dissipate.grpc.ValidateSessionRequest.getDefaultInstance();
    }

    @java.lang.Override
    public app.dissipate.grpc.ValidateSessionRequest build() {
      app.dissipate.grpc.ValidateSessionRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public app.dissipate.grpc.ValidateSessionRequest buildPartial() {
      app.dissipate.grpc.ValidateSessionRequest result = new app.dissipate.grpc.ValidateSessionRequest(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(app.dissipate.grpc.ValidateSessionRequest result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.sid_ = sid_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.otp_ = otp_;
      }
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof app.dissipate.grpc.ValidateSessionRequest) {
        return mergeFrom((app.dissipate.grpc.ValidateSessionRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(app.dissipate.grpc.ValidateSessionRequest other) {
      if (other == app.dissipate.grpc.ValidateSessionRequest.getDefaultInstance()) return this;
      if (!other.getSid().isEmpty()) {
        sid_ = other.sid_;
        bitField0_ |= 0x00000001;
        onChanged();
      }
      if (!other.getOtp().isEmpty()) {
        otp_ = other.otp_;
        bitField0_ |= 0x00000002;
        onChanged();
      }
      this.mergeUnknownFields(other.getUnknownFields());
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 10: {
              sid_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              otp_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            default: {
              if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                done = true; // was an endgroup tag
              }
              break;
            } // default:
          } // switch (tag)
        } // while (!done)
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.unwrapIOException();
      } finally {
        onChanged();
      } // finally
      return this;
    }
    private int bitField0_;

    private java.lang.Object sid_ = "";
    /**
     * <code>string sid = 1 [json_name = "sid", (.buf.validate.field) = { ... }</code>
     * @return The sid.
     */
    public java.lang.String getSid() {
      java.lang.Object ref = sid_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        sid_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string sid = 1 [json_name = "sid", (.buf.validate.field) = { ... }</code>
     * @return The bytes for sid.
     */
    public com.google.protobuf.ByteString
        getSidBytes() {
      java.lang.Object ref = sid_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        sid_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string sid = 1 [json_name = "sid", (.buf.validate.field) = { ... }</code>
     * @param value The sid to set.
     * @return This builder for chaining.
     */
    public Builder setSid(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      sid_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>string sid = 1 [json_name = "sid", (.buf.validate.field) = { ... }</code>
     * @return This builder for chaining.
     */
    public Builder clearSid() {
      sid_ = getDefaultInstance().getSid();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <code>string sid = 1 [json_name = "sid", (.buf.validate.field) = { ... }</code>
     * @param value The bytes for sid to set.
     * @return This builder for chaining.
     */
    public Builder setSidBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      sid_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }

    private java.lang.Object otp_ = "";
    /**
     * <code>string otp = 2 [json_name = "otp", (.buf.validate.field) = { ... }</code>
     * @return The otp.
     */
    public java.lang.String getOtp() {
      java.lang.Object ref = otp_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        otp_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string otp = 2 [json_name = "otp", (.buf.validate.field) = { ... }</code>
     * @return The bytes for otp.
     */
    public com.google.protobuf.ByteString
        getOtpBytes() {
      java.lang.Object ref = otp_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        otp_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string otp = 2 [json_name = "otp", (.buf.validate.field) = { ... }</code>
     * @param value The otp to set.
     * @return This builder for chaining.
     */
    public Builder setOtp(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      otp_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>string otp = 2 [json_name = "otp", (.buf.validate.field) = { ... }</code>
     * @return This builder for chaining.
     */
    public Builder clearOtp() {
      otp_ = getDefaultInstance().getOtp();
      bitField0_ = (bitField0_ & ~0x00000002);
      onChanged();
      return this;
    }
    /**
     * <code>string otp = 2 [json_name = "otp", (.buf.validate.field) = { ... }</code>
     * @param value The bytes for otp to set.
     * @return This builder for chaining.
     */
    public Builder setOtpBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      otp_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:ValidateSessionRequest)
  }

  // @@protoc_insertion_point(class_scope:ValidateSessionRequest)
  private static final app.dissipate.grpc.ValidateSessionRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new app.dissipate.grpc.ValidateSessionRequest();
  }

  public static app.dissipate.grpc.ValidateSessionRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ValidateSessionRequest>
      PARSER = new com.google.protobuf.AbstractParser<ValidateSessionRequest>() {
    @java.lang.Override
    public ValidateSessionRequest parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      Builder builder = newBuilder();
      try {
        builder.mergeFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(builder.buildPartial());
      } catch (com.google.protobuf.UninitializedMessageException e) {
        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(e)
            .setUnfinishedMessage(builder.buildPartial());
      }
      return builder.buildPartial();
    }
  };

  public static com.google.protobuf.Parser<ValidateSessionRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ValidateSessionRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public app.dissipate.grpc.ValidateSessionRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
