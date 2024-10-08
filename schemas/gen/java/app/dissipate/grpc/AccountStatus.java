// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: dissipate.proto
// Protobuf Java Version: 4.27.3

package app.dissipate.grpc;

/**
 * Protobuf enum {@code AccountStatus}
 */
public enum AccountStatus
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>ACCOUNT_STATUS_ACTIVE = 0;</code>
   */
  ACCOUNT_STATUS_ACTIVE(0),
  /**
   * <code>ACCOUNT_STATUS_DISABLED = 1;</code>
   */
  ACCOUNT_STATUS_DISABLED(1),
  /**
   * <code>ACCOUNT_STATUS_SUSPENDED = 2;</code>
   */
  ACCOUNT_STATUS_SUSPENDED(2),
  /**
   * <code>ACCOUNT_STATUS_BANNED = 3;</code>
   */
  ACCOUNT_STATUS_BANNED(3),
  UNRECOGNIZED(-1),
  ;

  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 27,
      /* patch= */ 3,
      /* suffix= */ "",
      AccountStatus.class.getName());
  }
  /**
   * <code>ACCOUNT_STATUS_ACTIVE = 0;</code>
   */
  public static final int ACCOUNT_STATUS_ACTIVE_VALUE = 0;
  /**
   * <code>ACCOUNT_STATUS_DISABLED = 1;</code>
   */
  public static final int ACCOUNT_STATUS_DISABLED_VALUE = 1;
  /**
   * <code>ACCOUNT_STATUS_SUSPENDED = 2;</code>
   */
  public static final int ACCOUNT_STATUS_SUSPENDED_VALUE = 2;
  /**
   * <code>ACCOUNT_STATUS_BANNED = 3;</code>
   */
  public static final int ACCOUNT_STATUS_BANNED_VALUE = 3;


  public final int getNumber() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalArgumentException(
          "Can't get the number of an unknown enum value.");
    }
    return value;
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   * @deprecated Use {@link #forNumber(int)} instead.
   */
  @java.lang.Deprecated
  public static AccountStatus valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static AccountStatus forNumber(int value) {
    switch (value) {
      case 0: return ACCOUNT_STATUS_ACTIVE;
      case 1: return ACCOUNT_STATUS_DISABLED;
      case 2: return ACCOUNT_STATUS_SUSPENDED;
      case 3: return ACCOUNT_STATUS_BANNED;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<AccountStatus>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      AccountStatus> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<AccountStatus>() {
          public AccountStatus findValueByNumber(int number) {
            return AccountStatus.forNumber(number);
          }
        };

  public final com.google.protobuf.Descriptors.EnumValueDescriptor
      getValueDescriptor() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalStateException(
          "Can't get the descriptor of an unrecognized enum value.");
    }
    return getDescriptor().getValues().get(ordinal());
  }
  public final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptorForType() {
    return getDescriptor();
  }
  public static final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptor() {
    return app.dissipate.grpc.DissipateProto.getDescriptor().getEnumTypes().get(0);
  }

  private static final AccountStatus[] VALUES = values();

  public static AccountStatus valueOf(
      com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
    if (desc.getType() != getDescriptor()) {
      throw new java.lang.IllegalArgumentException(
        "EnumValueDescriptor is not for this type.");
    }
    if (desc.getIndex() == -1) {
      return UNRECOGNIZED;
    }
    return VALUES[desc.getIndex()];
  }

  private final int value;

  private AccountStatus(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:AccountStatus)
}

