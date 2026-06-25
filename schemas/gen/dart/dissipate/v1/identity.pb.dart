//
//  Generated code. Do not modify.
//  source: dissipate/v1/identity.proto
//
// @dart = 2.12

// ignore_for_file: annotate_overrides, camel_case_types, comment_references
// ignore_for_file: constant_identifier_names, library_prefixes
// ignore_for_file: non_constant_identifier_names, prefer_final_fields
// ignore_for_file: unnecessary_import, unnecessary_this, unused_import

import 'dart:core' as $core;

import 'package:protobuf/protobuf.dart' as $pb;

import '../../google/protobuf/timestamp.pb.dart' as $4;

class CreateIdentityRequest extends $pb.GeneratedMessage {
  factory CreateIdentityRequest({
    $core.String? username,
    $core.String? name,
  }) {
    final $result = create();
    if (username != null) {
      $result.username = username;
    }
    if (name != null) {
      $result.name = name;
    }
    return $result;
  }
  CreateIdentityRequest._() : super();
  factory CreateIdentityRequest.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory CreateIdentityRequest.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);

  static final $pb.BuilderInfo _i = $pb.BuilderInfo(_omitMessageNames ? '' : 'CreateIdentityRequest', package: const $pb.PackageName(_omitMessageNames ? '' : 'dissipate.v1'), createEmptyInstance: create)
    ..aOS(1, _omitFieldNames ? '' : 'username')
    ..aOS(2, _omitFieldNames ? '' : 'name')
    ..hasRequiredFields = false
  ;

  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  CreateIdentityRequest clone() => CreateIdentityRequest()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  CreateIdentityRequest copyWith(void Function(CreateIdentityRequest) updates) => super.copyWith((message) => updates(message as CreateIdentityRequest)) as CreateIdentityRequest;

  $pb.BuilderInfo get info_ => _i;

  @$core.pragma('dart2js:noInline')
  static CreateIdentityRequest create() => CreateIdentityRequest._();
  CreateIdentityRequest createEmptyInstance() => create();
  static $pb.PbList<CreateIdentityRequest> createRepeated() => $pb.PbList<CreateIdentityRequest>();
  @$core.pragma('dart2js:noInline')
  static CreateIdentityRequest getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<CreateIdentityRequest>(create);
  static CreateIdentityRequest? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get username => $_getSZ(0);
  @$pb.TagNumber(1)
  set username($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasUsername() => $_has(0);
  @$pb.TagNumber(1)
  void clearUsername() => clearField(1);

  @$pb.TagNumber(2)
  $core.String get name => $_getSZ(1);
  @$pb.TagNumber(2)
  set name($core.String v) { $_setString(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasName() => $_has(1);
  @$pb.TagNumber(2)
  void clearName() => clearField(2);
}

class CreateIdentityResponse extends $pb.GeneratedMessage {
  factory CreateIdentityResponse({
    $core.String? sid,
    $core.String? iid,
    $core.String? username,
    $core.String? name,
    $4.Timestamp? created,
  }) {
    final $result = create();
    if (sid != null) {
      $result.sid = sid;
    }
    if (iid != null) {
      $result.iid = iid;
    }
    if (username != null) {
      $result.username = username;
    }
    if (name != null) {
      $result.name = name;
    }
    if (created != null) {
      $result.created = created;
    }
    return $result;
  }
  CreateIdentityResponse._() : super();
  factory CreateIdentityResponse.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory CreateIdentityResponse.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);

  static final $pb.BuilderInfo _i = $pb.BuilderInfo(_omitMessageNames ? '' : 'CreateIdentityResponse', package: const $pb.PackageName(_omitMessageNames ? '' : 'dissipate.v1'), createEmptyInstance: create)
    ..aOS(1, _omitFieldNames ? '' : 'sid')
    ..aOS(2, _omitFieldNames ? '' : 'iid')
    ..aOS(3, _omitFieldNames ? '' : 'username')
    ..aOS(4, _omitFieldNames ? '' : 'name')
    ..aOM<$4.Timestamp>(5, _omitFieldNames ? '' : 'created', subBuilder: $4.Timestamp.create)
    ..hasRequiredFields = false
  ;

  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  CreateIdentityResponse clone() => CreateIdentityResponse()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  CreateIdentityResponse copyWith(void Function(CreateIdentityResponse) updates) => super.copyWith((message) => updates(message as CreateIdentityResponse)) as CreateIdentityResponse;

  $pb.BuilderInfo get info_ => _i;

  @$core.pragma('dart2js:noInline')
  static CreateIdentityResponse create() => CreateIdentityResponse._();
  CreateIdentityResponse createEmptyInstance() => create();
  static $pb.PbList<CreateIdentityResponse> createRepeated() => $pb.PbList<CreateIdentityResponse>();
  @$core.pragma('dart2js:noInline')
  static CreateIdentityResponse getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<CreateIdentityResponse>(create);
  static CreateIdentityResponse? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get sid => $_getSZ(0);
  @$pb.TagNumber(1)
  set sid($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasSid() => $_has(0);
  @$pb.TagNumber(1)
  void clearSid() => clearField(1);

  @$pb.TagNumber(2)
  $core.String get iid => $_getSZ(1);
  @$pb.TagNumber(2)
  set iid($core.String v) { $_setString(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasIid() => $_has(1);
  @$pb.TagNumber(2)
  void clearIid() => clearField(2);

  @$pb.TagNumber(3)
  $core.String get username => $_getSZ(2);
  @$pb.TagNumber(3)
  set username($core.String v) { $_setString(2, v); }
  @$pb.TagNumber(3)
  $core.bool hasUsername() => $_has(2);
  @$pb.TagNumber(3)
  void clearUsername() => clearField(3);

  @$pb.TagNumber(4)
  $core.String get name => $_getSZ(3);
  @$pb.TagNumber(4)
  set name($core.String v) { $_setString(3, v); }
  @$pb.TagNumber(4)
  $core.bool hasName() => $_has(3);
  @$pb.TagNumber(4)
  void clearName() => clearField(4);

  @$pb.TagNumber(5)
  $4.Timestamp get created => $_getN(4);
  @$pb.TagNumber(5)
  set created($4.Timestamp v) { setField(5, v); }
  @$pb.TagNumber(5)
  $core.bool hasCreated() => $_has(4);
  @$pb.TagNumber(5)
  void clearCreated() => clearField(5);
  @$pb.TagNumber(5)
  $4.Timestamp ensureCreated() => $_ensure(4);
}

class ChangeIdentityRequest extends $pb.GeneratedMessage {
  factory ChangeIdentityRequest({
    $core.String? iid,
  }) {
    final $result = create();
    if (iid != null) {
      $result.iid = iid;
    }
    return $result;
  }
  ChangeIdentityRequest._() : super();
  factory ChangeIdentityRequest.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory ChangeIdentityRequest.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);

  static final $pb.BuilderInfo _i = $pb.BuilderInfo(_omitMessageNames ? '' : 'ChangeIdentityRequest', package: const $pb.PackageName(_omitMessageNames ? '' : 'dissipate.v1'), createEmptyInstance: create)
    ..aOS(1, _omitFieldNames ? '' : 'iid')
    ..hasRequiredFields = false
  ;

  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  ChangeIdentityRequest clone() => ChangeIdentityRequest()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  ChangeIdentityRequest copyWith(void Function(ChangeIdentityRequest) updates) => super.copyWith((message) => updates(message as ChangeIdentityRequest)) as ChangeIdentityRequest;

  $pb.BuilderInfo get info_ => _i;

  @$core.pragma('dart2js:noInline')
  static ChangeIdentityRequest create() => ChangeIdentityRequest._();
  ChangeIdentityRequest createEmptyInstance() => create();
  static $pb.PbList<ChangeIdentityRequest> createRepeated() => $pb.PbList<ChangeIdentityRequest>();
  @$core.pragma('dart2js:noInline')
  static ChangeIdentityRequest getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<ChangeIdentityRequest>(create);
  static ChangeIdentityRequest? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get iid => $_getSZ(0);
  @$pb.TagNumber(1)
  set iid($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasIid() => $_has(0);
  @$pb.TagNumber(1)
  void clearIid() => clearField(1);
}

class ChangeIdentityResponse extends $pb.GeneratedMessage {
  factory ChangeIdentityResponse({
    $core.String? sid,
    $core.String? iid,
    $core.String? username,
    $core.String? name,
    $4.Timestamp? created,
  }) {
    final $result = create();
    if (sid != null) {
      $result.sid = sid;
    }
    if (iid != null) {
      $result.iid = iid;
    }
    if (username != null) {
      $result.username = username;
    }
    if (name != null) {
      $result.name = name;
    }
    if (created != null) {
      $result.created = created;
    }
    return $result;
  }
  ChangeIdentityResponse._() : super();
  factory ChangeIdentityResponse.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory ChangeIdentityResponse.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);

  static final $pb.BuilderInfo _i = $pb.BuilderInfo(_omitMessageNames ? '' : 'ChangeIdentityResponse', package: const $pb.PackageName(_omitMessageNames ? '' : 'dissipate.v1'), createEmptyInstance: create)
    ..aOS(1, _omitFieldNames ? '' : 'sid')
    ..aOS(2, _omitFieldNames ? '' : 'iid')
    ..aOS(3, _omitFieldNames ? '' : 'username')
    ..aOS(4, _omitFieldNames ? '' : 'name')
    ..aOM<$4.Timestamp>(5, _omitFieldNames ? '' : 'created', subBuilder: $4.Timestamp.create)
    ..hasRequiredFields = false
  ;

  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  ChangeIdentityResponse clone() => ChangeIdentityResponse()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  ChangeIdentityResponse copyWith(void Function(ChangeIdentityResponse) updates) => super.copyWith((message) => updates(message as ChangeIdentityResponse)) as ChangeIdentityResponse;

  $pb.BuilderInfo get info_ => _i;

  @$core.pragma('dart2js:noInline')
  static ChangeIdentityResponse create() => ChangeIdentityResponse._();
  ChangeIdentityResponse createEmptyInstance() => create();
  static $pb.PbList<ChangeIdentityResponse> createRepeated() => $pb.PbList<ChangeIdentityResponse>();
  @$core.pragma('dart2js:noInline')
  static ChangeIdentityResponse getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<ChangeIdentityResponse>(create);
  static ChangeIdentityResponse? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get sid => $_getSZ(0);
  @$pb.TagNumber(1)
  set sid($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasSid() => $_has(0);
  @$pb.TagNumber(1)
  void clearSid() => clearField(1);

  @$pb.TagNumber(2)
  $core.String get iid => $_getSZ(1);
  @$pb.TagNumber(2)
  set iid($core.String v) { $_setString(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasIid() => $_has(1);
  @$pb.TagNumber(2)
  void clearIid() => clearField(2);

  @$pb.TagNumber(3)
  $core.String get username => $_getSZ(2);
  @$pb.TagNumber(3)
  set username($core.String v) { $_setString(2, v); }
  @$pb.TagNumber(3)
  $core.bool hasUsername() => $_has(2);
  @$pb.TagNumber(3)
  void clearUsername() => clearField(3);

  @$pb.TagNumber(4)
  $core.String get name => $_getSZ(3);
  @$pb.TagNumber(4)
  set name($core.String v) { $_setString(3, v); }
  @$pb.TagNumber(4)
  $core.bool hasName() => $_has(3);
  @$pb.TagNumber(4)
  void clearName() => clearField(4);

  @$pb.TagNumber(5)
  $4.Timestamp get created => $_getN(4);
  @$pb.TagNumber(5)
  set created($4.Timestamp v) { setField(5, v); }
  @$pb.TagNumber(5)
  $core.bool hasCreated() => $_has(4);
  @$pb.TagNumber(5)
  void clearCreated() => clearField(5);
  @$pb.TagNumber(5)
  $4.Timestamp ensureCreated() => $_ensure(4);
}


const _omitFieldNames = $core.bool.fromEnvironment('protobuf.omit_field_names');
const _omitMessageNames = $core.bool.fromEnvironment('protobuf.omit_message_names');
