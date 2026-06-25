//
//  Generated code. Do not modify.
//  source: dissipate/v1/common.proto
//
// @dart = 2.12

// ignore_for_file: annotate_overrides, camel_case_types, comment_references
// ignore_for_file: constant_identifier_names, library_prefixes
// ignore_for_file: non_constant_identifier_names, prefer_final_fields
// ignore_for_file: unnecessary_import, unnecessary_this, unused_import

import 'dart:core' as $core;

import 'package:protobuf/protobuf.dart' as $pb;

class Role extends $pb.ProtobufEnum {
  static const Role ROLE_UNSPECIFIED = Role._(0, _omitEnumNames ? '' : 'ROLE_UNSPECIFIED');
  static const Role ROLE_USER = Role._(1, _omitEnumNames ? '' : 'ROLE_USER');
  static const Role ROLE_VERIFIED = Role._(2, _omitEnumNames ? '' : 'ROLE_VERIFIED');
  static const Role ROLE_ADMIN = Role._(3, _omitEnumNames ? '' : 'ROLE_ADMIN');

  static const $core.List<Role> values = <Role> [
    ROLE_UNSPECIFIED,
    ROLE_USER,
    ROLE_VERIFIED,
    ROLE_ADMIN,
  ];

  static final $core.Map<$core.int, Role> _byValue = $pb.ProtobufEnum.initByValue(values);
  static Role? valueOf($core.int value) => _byValue[value];

  const Role._($core.int v, $core.String n) : super(v, n);
}

class AccountStatus extends $pb.ProtobufEnum {
  static const AccountStatus ACCOUNT_STATUS_UNSPECIFIED = AccountStatus._(0, _omitEnumNames ? '' : 'ACCOUNT_STATUS_UNSPECIFIED');
  static const AccountStatus ACCOUNT_STATUS_ANONYMOUS = AccountStatus._(1, _omitEnumNames ? '' : 'ACCOUNT_STATUS_ANONYMOUS');
  static const AccountStatus ACCOUNT_STATUS_ACTIVE = AccountStatus._(2, _omitEnumNames ? '' : 'ACCOUNT_STATUS_ACTIVE');
  static const AccountStatus ACCOUNT_STATUS_DISABLED = AccountStatus._(3, _omitEnumNames ? '' : 'ACCOUNT_STATUS_DISABLED');
  static const AccountStatus ACCOUNT_STATUS_SUSPENDED = AccountStatus._(4, _omitEnumNames ? '' : 'ACCOUNT_STATUS_SUSPENDED');
  static const AccountStatus ACCOUNT_STATUS_BANNED = AccountStatus._(5, _omitEnumNames ? '' : 'ACCOUNT_STATUS_BANNED');

  static const $core.List<AccountStatus> values = <AccountStatus> [
    ACCOUNT_STATUS_UNSPECIFIED,
    ACCOUNT_STATUS_ANONYMOUS,
    ACCOUNT_STATUS_ACTIVE,
    ACCOUNT_STATUS_DISABLED,
    ACCOUNT_STATUS_SUSPENDED,
    ACCOUNT_STATUS_BANNED,
  ];

  static final $core.Map<$core.int, AccountStatus> _byValue = $pb.ProtobufEnum.initByValue(values);
  static AccountStatus? valueOf($core.int value) => _byValue[value];

  const AccountStatus._($core.int v, $core.String n) : super(v, n);
}


const _omitEnumNames = $core.bool.fromEnvironment('protobuf.omit_enum_names');
