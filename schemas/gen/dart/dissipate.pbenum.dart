//
//  Generated code. Do not modify.
//  source: dissipate.proto
//
// @dart = 2.12

// ignore_for_file: annotate_overrides, camel_case_types, comment_references
// ignore_for_file: constant_identifier_names, library_prefixes
// ignore_for_file: non_constant_identifier_names, prefer_final_fields
// ignore_for_file: unnecessary_import, unnecessary_this, unused_import

import 'dart:core' as $core;

import 'package:protobuf/protobuf.dart' as $pb;

class AccountStatus extends $pb.ProtobufEnum {
  static const AccountStatus ACCOUNT_STATUS_ACTIVE = AccountStatus._(0, _omitEnumNames ? '' : 'ACCOUNT_STATUS_ACTIVE');
  static const AccountStatus ACCOUNT_STATUS_DISABLED = AccountStatus._(1, _omitEnumNames ? '' : 'ACCOUNT_STATUS_DISABLED');
  static const AccountStatus ACCOUNT_STATUS_SUSPENDED = AccountStatus._(2, _omitEnumNames ? '' : 'ACCOUNT_STATUS_SUSPENDED');
  static const AccountStatus ACCOUNT_STATUS_BANNED = AccountStatus._(3, _omitEnumNames ? '' : 'ACCOUNT_STATUS_BANNED');

  static const $core.List<AccountStatus> values = <AccountStatus> [
    ACCOUNT_STATUS_ACTIVE,
    ACCOUNT_STATUS_DISABLED,
    ACCOUNT_STATUS_SUSPENDED,
    ACCOUNT_STATUS_BANNED,
  ];

  static final $core.Map<$core.int, AccountStatus> _byValue = $pb.ProtobufEnum.initByValue(values);
  static AccountStatus? valueOf($core.int value) => _byValue[value];

  const AccountStatus._($core.int v, $core.String n) : super(v, n);
}

class RegisterResponseResult extends $pb.ProtobufEnum {
  static const RegisterResponseResult REGISTER_RESPONSE_RESULT_ERROR_UNSPECIFIED = RegisterResponseResult._(0, _omitEnumNames ? '' : 'REGISTER_RESPONSE_RESULT_ERROR_UNSPECIFIED');
  static const RegisterResponseResult REGISTER_RESPONSE_RESULT_EMAIL_SENT = RegisterResponseResult._(1, _omitEnumNames ? '' : 'REGISTER_RESPONSE_RESULT_EMAIL_SENT');
  static const RegisterResponseResult REGISTER_RESPONSE_RESULT_PHONE_SENT = RegisterResponseResult._(2, _omitEnumNames ? '' : 'REGISTER_RESPONSE_RESULT_PHONE_SENT');

  static const $core.List<RegisterResponseResult> values = <RegisterResponseResult> [
    REGISTER_RESPONSE_RESULT_ERROR_UNSPECIFIED,
    REGISTER_RESPONSE_RESULT_EMAIL_SENT,
    REGISTER_RESPONSE_RESULT_PHONE_SENT,
  ];

  static final $core.Map<$core.int, RegisterResponseResult> _byValue = $pb.ProtobufEnum.initByValue(values);
  static RegisterResponseResult? valueOf($core.int value) => _byValue[value];

  const RegisterResponseResult._($core.int v, $core.String n) : super(v, n);
}


const _omitEnumNames = $core.bool.fromEnvironment('protobuf.omit_enum_names');
