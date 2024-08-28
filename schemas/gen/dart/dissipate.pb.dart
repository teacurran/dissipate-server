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

import 'dissipate.pbenum.dart';
import 'google/protobuf/timestamp.pb.dart' as $1;

export 'dissipate.pbenum.dart';

class CreateHandleRequest extends $pb.GeneratedMessage {
  factory CreateHandleRequest({
    $core.String? handle,
  }) {
    final $result = create();
    if (handle != null) {
      $result.handle = handle;
    }
    return $result;
  }
  CreateHandleRequest._() : super();
  factory CreateHandleRequest.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory CreateHandleRequest.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);

  static final $pb.BuilderInfo _i = $pb.BuilderInfo(_omitMessageNames ? '' : 'CreateHandleRequest', createEmptyInstance: create)
    ..aOS(1, _omitFieldNames ? '' : 'handle')
    ..hasRequiredFields = false
  ;

  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  CreateHandleRequest clone() => CreateHandleRequest()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  CreateHandleRequest copyWith(void Function(CreateHandleRequest) updates) => super.copyWith((message) => updates(message as CreateHandleRequest)) as CreateHandleRequest;

  $pb.BuilderInfo get info_ => _i;

  @$core.pragma('dart2js:noInline')
  static CreateHandleRequest create() => CreateHandleRequest._();
  CreateHandleRequest createEmptyInstance() => create();
  static $pb.PbList<CreateHandleRequest> createRepeated() => $pb.PbList<CreateHandleRequest>();
  @$core.pragma('dart2js:noInline')
  static CreateHandleRequest getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<CreateHandleRequest>(create);
  static CreateHandleRequest? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get handle => $_getSZ(0);
  @$pb.TagNumber(1)
  set handle($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasHandle() => $_has(0);
  @$pb.TagNumber(1)
  void clearHandle() => clearField(1);
}

class CreateHandleResponse extends $pb.GeneratedMessage {
  factory CreateHandleResponse({
    $core.String? id,
    $core.String? handle,
    $1.Timestamp? created,
    $1.Timestamp? lastSeen,
  }) {
    final $result = create();
    if (id != null) {
      $result.id = id;
    }
    if (handle != null) {
      $result.handle = handle;
    }
    if (created != null) {
      $result.created = created;
    }
    if (lastSeen != null) {
      $result.lastSeen = lastSeen;
    }
    return $result;
  }
  CreateHandleResponse._() : super();
  factory CreateHandleResponse.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory CreateHandleResponse.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);

  static final $pb.BuilderInfo _i = $pb.BuilderInfo(_omitMessageNames ? '' : 'CreateHandleResponse', createEmptyInstance: create)
    ..aOS(1, _omitFieldNames ? '' : 'id')
    ..aOS(2, _omitFieldNames ? '' : 'handle')
    ..aOM<$1.Timestamp>(3, _omitFieldNames ? '' : 'created', subBuilder: $1.Timestamp.create)
    ..aOM<$1.Timestamp>(4, _omitFieldNames ? '' : 'lastSeen', subBuilder: $1.Timestamp.create)
    ..hasRequiredFields = false
  ;

  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  CreateHandleResponse clone() => CreateHandleResponse()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  CreateHandleResponse copyWith(void Function(CreateHandleResponse) updates) => super.copyWith((message) => updates(message as CreateHandleResponse)) as CreateHandleResponse;

  $pb.BuilderInfo get info_ => _i;

  @$core.pragma('dart2js:noInline')
  static CreateHandleResponse create() => CreateHandleResponse._();
  CreateHandleResponse createEmptyInstance() => create();
  static $pb.PbList<CreateHandleResponse> createRepeated() => $pb.PbList<CreateHandleResponse>();
  @$core.pragma('dart2js:noInline')
  static CreateHandleResponse getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<CreateHandleResponse>(create);
  static CreateHandleResponse? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get id => $_getSZ(0);
  @$pb.TagNumber(1)
  set id($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasId() => $_has(0);
  @$pb.TagNumber(1)
  void clearId() => clearField(1);

  @$pb.TagNumber(2)
  $core.String get handle => $_getSZ(1);
  @$pb.TagNumber(2)
  set handle($core.String v) { $_setString(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasHandle() => $_has(1);
  @$pb.TagNumber(2)
  void clearHandle() => clearField(2);

  @$pb.TagNumber(3)
  $1.Timestamp get created => $_getN(2);
  @$pb.TagNumber(3)
  set created($1.Timestamp v) { setField(3, v); }
  @$pb.TagNumber(3)
  $core.bool hasCreated() => $_has(2);
  @$pb.TagNumber(3)
  void clearCreated() => clearField(3);
  @$pb.TagNumber(3)
  $1.Timestamp ensureCreated() => $_ensure(2);

  @$pb.TagNumber(4)
  $1.Timestamp get lastSeen => $_getN(3);
  @$pb.TagNumber(4)
  set lastSeen($1.Timestamp v) { setField(4, v); }
  @$pb.TagNumber(4)
  $core.bool hasLastSeen() => $_has(3);
  @$pb.TagNumber(4)
  void clearLastSeen() => clearField(4);
  @$pb.TagNumber(4)
  $1.Timestamp ensureLastSeen() => $_ensure(3);
}

class RegisterRequest extends $pb.GeneratedMessage {
  factory RegisterRequest({
    $core.String? locale,
    $core.String? email,
    $core.String? phoneNumber,
  }) {
    final $result = create();
    if (locale != null) {
      $result.locale = locale;
    }
    if (email != null) {
      $result.email = email;
    }
    if (phoneNumber != null) {
      $result.phoneNumber = phoneNumber;
    }
    return $result;
  }
  RegisterRequest._() : super();
  factory RegisterRequest.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory RegisterRequest.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);

  static final $pb.BuilderInfo _i = $pb.BuilderInfo(_omitMessageNames ? '' : 'RegisterRequest', createEmptyInstance: create)
    ..aOS(1, _omitFieldNames ? '' : 'locale')
    ..aOS(2, _omitFieldNames ? '' : 'email')
    ..aOS(3, _omitFieldNames ? '' : 'phoneNumber')
    ..hasRequiredFields = false
  ;

  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  RegisterRequest clone() => RegisterRequest()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  RegisterRequest copyWith(void Function(RegisterRequest) updates) => super.copyWith((message) => updates(message as RegisterRequest)) as RegisterRequest;

  $pb.BuilderInfo get info_ => _i;

  @$core.pragma('dart2js:noInline')
  static RegisterRequest create() => RegisterRequest._();
  RegisterRequest createEmptyInstance() => create();
  static $pb.PbList<RegisterRequest> createRepeated() => $pb.PbList<RegisterRequest>();
  @$core.pragma('dart2js:noInline')
  static RegisterRequest getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<RegisterRequest>(create);
  static RegisterRequest? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get locale => $_getSZ(0);
  @$pb.TagNumber(1)
  set locale($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasLocale() => $_has(0);
  @$pb.TagNumber(1)
  void clearLocale() => clearField(1);

  @$pb.TagNumber(2)
  $core.String get email => $_getSZ(1);
  @$pb.TagNumber(2)
  set email($core.String v) { $_setString(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasEmail() => $_has(1);
  @$pb.TagNumber(2)
  void clearEmail() => clearField(2);

  @$pb.TagNumber(3)
  $core.String get phoneNumber => $_getSZ(2);
  @$pb.TagNumber(3)
  set phoneNumber($core.String v) { $_setString(2, v); }
  @$pb.TagNumber(3)
  $core.bool hasPhoneNumber() => $_has(2);
  @$pb.TagNumber(3)
  void clearPhoneNumber() => clearField(3);
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

  static final $pb.BuilderInfo _i = $pb.BuilderInfo(_omitMessageNames ? '' : 'ApiError', createEmptyInstance: create)
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

class RegisterResponse extends $pb.GeneratedMessage {
  factory RegisterResponse({
    RegisterResponseResult? result,
    $core.String? sid,
    ApiError? error,
  }) {
    final $result = create();
    if (result != null) {
      $result.result = result;
    }
    if (sid != null) {
      $result.sid = sid;
    }
    if (error != null) {
      $result.error = error;
    }
    return $result;
  }
  RegisterResponse._() : super();
  factory RegisterResponse.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory RegisterResponse.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);

  static final $pb.BuilderInfo _i = $pb.BuilderInfo(_omitMessageNames ? '' : 'RegisterResponse', createEmptyInstance: create)
    ..e<RegisterResponseResult>(1, _omitFieldNames ? '' : 'result', $pb.PbFieldType.OE, defaultOrMaker: RegisterResponseResult.REGISTER_RESPONSE_RESULT_ERROR_UNSPECIFIED, valueOf: RegisterResponseResult.valueOf, enumValues: RegisterResponseResult.values)
    ..aOS(2, _omitFieldNames ? '' : 'sid')
    ..aOM<ApiError>(3, _omitFieldNames ? '' : 'error', subBuilder: ApiError.create)
    ..hasRequiredFields = false
  ;

  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  RegisterResponse clone() => RegisterResponse()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  RegisterResponse copyWith(void Function(RegisterResponse) updates) => super.copyWith((message) => updates(message as RegisterResponse)) as RegisterResponse;

  $pb.BuilderInfo get info_ => _i;

  @$core.pragma('dart2js:noInline')
  static RegisterResponse create() => RegisterResponse._();
  RegisterResponse createEmptyInstance() => create();
  static $pb.PbList<RegisterResponse> createRepeated() => $pb.PbList<RegisterResponse>();
  @$core.pragma('dart2js:noInline')
  static RegisterResponse getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<RegisterResponse>(create);
  static RegisterResponse? _defaultInstance;

  @$pb.TagNumber(1)
  RegisterResponseResult get result => $_getN(0);
  @$pb.TagNumber(1)
  set result(RegisterResponseResult v) { setField(1, v); }
  @$pb.TagNumber(1)
  $core.bool hasResult() => $_has(0);
  @$pb.TagNumber(1)
  void clearResult() => clearField(1);

  @$pb.TagNumber(2)
  $core.String get sid => $_getSZ(1);
  @$pb.TagNumber(2)
  set sid($core.String v) { $_setString(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasSid() => $_has(1);
  @$pb.TagNumber(2)
  void clearSid() => clearField(2);

  @$pb.TagNumber(3)
  ApiError get error => $_getN(2);
  @$pb.TagNumber(3)
  set error(ApiError v) { setField(3, v); }
  @$pb.TagNumber(3)
  $core.bool hasError() => $_has(2);
  @$pb.TagNumber(3)
  void clearError() => clearField(3);
  @$pb.TagNumber(3)
  ApiError ensureError() => $_ensure(2);
}

class ValidateSessionRequest extends $pb.GeneratedMessage {
  factory ValidateSessionRequest({
    $core.String? locale,
    $core.String? sid,
    $core.String? otp,
  }) {
    final $result = create();
    if (locale != null) {
      $result.locale = locale;
    }
    if (sid != null) {
      $result.sid = sid;
    }
    if (otp != null) {
      $result.otp = otp;
    }
    return $result;
  }
  ValidateSessionRequest._() : super();
  factory ValidateSessionRequest.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory ValidateSessionRequest.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);

  static final $pb.BuilderInfo _i = $pb.BuilderInfo(_omitMessageNames ? '' : 'ValidateSessionRequest', createEmptyInstance: create)
    ..aOS(1, _omitFieldNames ? '' : 'locale')
    ..aOS(2, _omitFieldNames ? '' : 'sid')
    ..aOS(3, _omitFieldNames ? '' : 'otp')
    ..hasRequiredFields = false
  ;

  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  ValidateSessionRequest clone() => ValidateSessionRequest()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  ValidateSessionRequest copyWith(void Function(ValidateSessionRequest) updates) => super.copyWith((message) => updates(message as ValidateSessionRequest)) as ValidateSessionRequest;

  $pb.BuilderInfo get info_ => _i;

  @$core.pragma('dart2js:noInline')
  static ValidateSessionRequest create() => ValidateSessionRequest._();
  ValidateSessionRequest createEmptyInstance() => create();
  static $pb.PbList<ValidateSessionRequest> createRepeated() => $pb.PbList<ValidateSessionRequest>();
  @$core.pragma('dart2js:noInline')
  static ValidateSessionRequest getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<ValidateSessionRequest>(create);
  static ValidateSessionRequest? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get locale => $_getSZ(0);
  @$pb.TagNumber(1)
  set locale($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasLocale() => $_has(0);
  @$pb.TagNumber(1)
  void clearLocale() => clearField(1);

  @$pb.TagNumber(2)
  $core.String get sid => $_getSZ(1);
  @$pb.TagNumber(2)
  set sid($core.String v) { $_setString(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasSid() => $_has(1);
  @$pb.TagNumber(2)
  void clearSid() => clearField(2);

  @$pb.TagNumber(3)
  $core.String get otp => $_getSZ(2);
  @$pb.TagNumber(3)
  set otp($core.String v) { $_setString(2, v); }
  @$pb.TagNumber(3)
  $core.bool hasOtp() => $_has(2);
  @$pb.TagNumber(3)
  void clearOtp() => clearField(3);
}

class ValidateSessionResponse extends $pb.GeneratedMessage {
  factory ValidateSessionResponse({
    $core.bool? valid,
    $core.String? errorMessage,
  }) {
    final $result = create();
    if (valid != null) {
      $result.valid = valid;
    }
    if (errorMessage != null) {
      $result.errorMessage = errorMessage;
    }
    return $result;
  }
  ValidateSessionResponse._() : super();
  factory ValidateSessionResponse.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory ValidateSessionResponse.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);

  static final $pb.BuilderInfo _i = $pb.BuilderInfo(_omitMessageNames ? '' : 'ValidateSessionResponse', createEmptyInstance: create)
    ..aOB(1, _omitFieldNames ? '' : 'valid')
    ..aOS(2, _omitFieldNames ? '' : 'errorMessage')
    ..hasRequiredFields = false
  ;

  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  ValidateSessionResponse clone() => ValidateSessionResponse()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  ValidateSessionResponse copyWith(void Function(ValidateSessionResponse) updates) => super.copyWith((message) => updates(message as ValidateSessionResponse)) as ValidateSessionResponse;

  $pb.BuilderInfo get info_ => _i;

  @$core.pragma('dart2js:noInline')
  static ValidateSessionResponse create() => ValidateSessionResponse._();
  ValidateSessionResponse createEmptyInstance() => create();
  static $pb.PbList<ValidateSessionResponse> createRepeated() => $pb.PbList<ValidateSessionResponse>();
  @$core.pragma('dart2js:noInline')
  static ValidateSessionResponse getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<ValidateSessionResponse>(create);
  static ValidateSessionResponse? _defaultInstance;

  @$pb.TagNumber(1)
  $core.bool get valid => $_getBF(0);
  @$pb.TagNumber(1)
  set valid($core.bool v) { $_setBool(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasValid() => $_has(0);
  @$pb.TagNumber(1)
  void clearValid() => clearField(1);

  @$pb.TagNumber(2)
  $core.String get errorMessage => $_getSZ(1);
  @$pb.TagNumber(2)
  set errorMessage($core.String v) { $_setString(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasErrorMessage() => $_has(1);
  @$pb.TagNumber(2)
  void clearErrorMessage() => clearField(2);
}


const _omitFieldNames = $core.bool.fromEnvironment('protobuf.omit_field_names');
const _omitMessageNames = $core.bool.fromEnvironment('protobuf.omit_message_names');
