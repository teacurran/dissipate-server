//
//  Generated code. Do not modify.
//  source: dissipate/v1/account.proto
//
// @dart = 2.12

// ignore_for_file: annotate_overrides, camel_case_types, comment_references
// ignore_for_file: constant_identifier_names, library_prefixes
// ignore_for_file: non_constant_identifier_names, prefer_final_fields
// ignore_for_file: unnecessary_import, unnecessary_this, unused_import

import 'dart:core' as $core;

import 'package:protobuf/protobuf.dart' as $pb;

class RegisterResponseResult extends $pb.ProtobufEnum {
  static const RegisterResponseResult REGISTER_RESPONSE_RESULT_UNSPECIFIED = RegisterResponseResult._(0, _omitEnumNames ? '' : 'REGISTER_RESPONSE_RESULT_UNSPECIFIED');
  static const RegisterResponseResult REGISTER_RESPONSE_RESULT_SESSION_CREATED = RegisterResponseResult._(1, _omitEnumNames ? '' : 'REGISTER_RESPONSE_RESULT_SESSION_CREATED');
  static const RegisterResponseResult REGISTER_RESPONSE_RESULT_EMAIL_SENT = RegisterResponseResult._(2, _omitEnumNames ? '' : 'REGISTER_RESPONSE_RESULT_EMAIL_SENT');
  static const RegisterResponseResult REGISTER_RESPONSE_RESULT_PHONE_SENT = RegisterResponseResult._(3, _omitEnumNames ? '' : 'REGISTER_RESPONSE_RESULT_PHONE_SENT');

  static const $core.List<RegisterResponseResult> values = <RegisterResponseResult> [
    REGISTER_RESPONSE_RESULT_UNSPECIFIED,
    REGISTER_RESPONSE_RESULT_SESSION_CREATED,
    REGISTER_RESPONSE_RESULT_EMAIL_SENT,
    REGISTER_RESPONSE_RESULT_PHONE_SENT,
  ];

  static final $core.Map<$core.int, RegisterResponseResult> _byValue = $pb.ProtobufEnum.initByValue(values);
  static RegisterResponseResult? valueOf($core.int value) => _byValue[value];

  const RegisterResponseResult._($core.int v, $core.String n) : super(v, n);
}


const _omitEnumNames = $core.bool.fromEnvironment('protobuf.omit_enum_names');
