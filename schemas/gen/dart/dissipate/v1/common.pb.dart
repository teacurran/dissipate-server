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

import 'common.pbenum.dart';

export 'common.pbenum.dart';

class MethodPolicy extends $pb.GeneratedMessage {
  factory MethodPolicy({
    $core.bool? allowUnauthenticated,
    $core.bool? allowApp,
    $core.Iterable<$core.String>? scopes,
    Role? minRole,
    $core.int? cost,
  }) {
    final $result = create();
    if (allowUnauthenticated != null) {
      $result.allowUnauthenticated = allowUnauthenticated;
    }
    if (allowApp != null) {
      $result.allowApp = allowApp;
    }
    if (scopes != null) {
      $result.scopes.addAll(scopes);
    }
    if (minRole != null) {
      $result.minRole = minRole;
    }
    if (cost != null) {
      $result.cost = cost;
    }
    return $result;
  }
  MethodPolicy._() : super();
  factory MethodPolicy.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory MethodPolicy.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);

  static final $pb.BuilderInfo _i = $pb.BuilderInfo(_omitMessageNames ? '' : 'MethodPolicy', package: const $pb.PackageName(_omitMessageNames ? '' : 'dissipate.v1'), createEmptyInstance: create)
    ..aOB(1, _omitFieldNames ? '' : 'allowUnauthenticated')
    ..aOB(2, _omitFieldNames ? '' : 'allowApp')
    ..pPS(3, _omitFieldNames ? '' : 'scopes')
    ..e<Role>(4, _omitFieldNames ? '' : 'minRole', $pb.PbFieldType.OE, defaultOrMaker: Role.ROLE_UNSPECIFIED, valueOf: Role.valueOf, enumValues: Role.values)
    ..a<$core.int>(5, _omitFieldNames ? '' : 'cost', $pb.PbFieldType.OU3)
    ..hasRequiredFields = false
  ;

  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  MethodPolicy clone() => MethodPolicy()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  MethodPolicy copyWith(void Function(MethodPolicy) updates) => super.copyWith((message) => updates(message as MethodPolicy)) as MethodPolicy;

  $pb.BuilderInfo get info_ => _i;

  @$core.pragma('dart2js:noInline')
  static MethodPolicy create() => MethodPolicy._();
  MethodPolicy createEmptyInstance() => create();
  static $pb.PbList<MethodPolicy> createRepeated() => $pb.PbList<MethodPolicy>();
  @$core.pragma('dart2js:noInline')
  static MethodPolicy getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<MethodPolicy>(create);
  static MethodPolicy? _defaultInstance;

  /// No authentication required at all (e.g. Register, ValidateSession). When
  /// true, min_role / scopes / allow_app are ignored.
  @$pb.TagNumber(1)
  $core.bool get allowUnauthenticated => $_getBF(0);
  @$pb.TagNumber(1)
  set allowUnauthenticated($core.bool v) { $_setBool(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasAllowUnauthenticated() => $_has(0);
  @$pb.TagNumber(1)
  void clearAllowUnauthenticated() => clearField(1);

  /// Whether third-party registered API apps (not just first-party end users)
  /// may call this method. Default false => end-user sessions only.
  @$pb.TagNumber(2)
  $core.bool get allowApp => $_getBF(1);
  @$pb.TagNumber(2)
  set allowApp($core.bool v) { $_setBool(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasAllowApp() => $_has(1);
  @$pb.TagNumber(2)
  void clearAllowApp() => clearField(2);

  /// Resource:action scopes a third-party app must hold to call this method,
  /// e.g. "posts:write", "follows:read". Only meaningful when allow_app = true.
  @$pb.TagNumber(3)
  $core.List<$core.String> get scopes => $_getList(2);

  /// Minimum end-user role required (ignored when allow_unauthenticated = true).
  @$pb.TagNumber(4)
  Role get minRole => $_getN(3);
  @$pb.TagNumber(4)
  set minRole(Role v) { setField(4, v); }
  @$pb.TagNumber(4)
  $core.bool hasMinRole() => $_has(3);
  @$pb.TagNumber(4)
  void clearMinRole() => clearField(4);

  /// Rate-limit weight for this method; 0 is treated as the default weight (1).
  /// Expensive methods (feeds, search) cost more than cheap ones (a like).
  @$pb.TagNumber(5)
  $core.int get cost => $_getIZ(4);
  @$pb.TagNumber(5)
  set cost($core.int v) { $_setUnsignedInt32(4, v); }
  @$pb.TagNumber(5)
  $core.bool hasCost() => $_has(4);
  @$pb.TagNumber(5)
  void clearCost() => clearField(5);
}

class ApiError extends $pb.GeneratedMessage {
  factory ApiError({
    $core.String? code,
    $core.String? message,
  }) {
    final $result = create();
    if (code != null) {
      $result.code = code;
    }
    if (message != null) {
      $result.message = message;
    }
    return $result;
  }
  ApiError._() : super();
  factory ApiError.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory ApiError.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);

  static final $pb.BuilderInfo _i = $pb.BuilderInfo(_omitMessageNames ? '' : 'ApiError', package: const $pb.PackageName(_omitMessageNames ? '' : 'dissipate.v1'), createEmptyInstance: create)
    ..aOS(1, _omitFieldNames ? '' : 'code')
    ..aOS(2, _omitFieldNames ? '' : 'message')
    ..hasRequiredFields = false
  ;

  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  ApiError clone() => ApiError()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  ApiError copyWith(void Function(ApiError) updates) => super.copyWith((message) => updates(message as ApiError)) as ApiError;

  $pb.BuilderInfo get info_ => _i;

  @$core.pragma('dart2js:noInline')
  static ApiError create() => ApiError._();
  ApiError createEmptyInstance() => create();
  static $pb.PbList<ApiError> createRepeated() => $pb.PbList<ApiError>();
  @$core.pragma('dart2js:noInline')
  static ApiError getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<ApiError>(create);
  static ApiError? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get code => $_getSZ(0);
  @$pb.TagNumber(1)
  set code($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasCode() => $_has(0);
  @$pb.TagNumber(1)
  void clearCode() => clearField(1);

  @$pb.TagNumber(2)
  $core.String get message => $_getSZ(1);
  @$pb.TagNumber(2)
  set message($core.String v) { $_setString(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasMessage() => $_has(1);
  @$pb.TagNumber(2)
  void clearMessage() => clearField(2);
}

class Common {
  static final policy = $pb.Extension<MethodPolicy>(_omitMessageNames ? '' : 'google.protobuf.MethodOptions', _omitFieldNames ? '' : 'policy', 50000, $pb.PbFieldType.OM, defaultOrMaker: MethodPolicy.getDefault, subBuilder: MethodPolicy.create);
  static void registerAllExtensions($pb.ExtensionRegistry registry) {
    registry.add(policy);
  }
}


const _omitFieldNames = $core.bool.fromEnvironment('protobuf.omit_field_names');
const _omitMessageNames = $core.bool.fromEnvironment('protobuf.omit_message_names');
