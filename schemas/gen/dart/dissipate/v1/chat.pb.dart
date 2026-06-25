//
//  Generated code. Do not modify.
//  source: dissipate/v1/chat.proto
//
// @dart = 2.12

// ignore_for_file: annotate_overrides, camel_case_types, comment_references
// ignore_for_file: constant_identifier_names, library_prefixes
// ignore_for_file: non_constant_identifier_names, prefer_final_fields
// ignore_for_file: unnecessary_import, unnecessary_this, unused_import

import 'dart:core' as $core;

import 'package:protobuf/protobuf.dart' as $pb;

import '../../google/protobuf/timestamp.pb.dart' as $4;

class GetChatsRequest extends $pb.GeneratedMessage {
  factory GetChatsRequest() => create();
  GetChatsRequest._() : super();
  factory GetChatsRequest.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory GetChatsRequest.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);

  static final $pb.BuilderInfo _i = $pb.BuilderInfo(_omitMessageNames ? '' : 'GetChatsRequest', package: const $pb.PackageName(_omitMessageNames ? '' : 'dissipate.v1'), createEmptyInstance: create)
    ..hasRequiredFields = false
  ;

  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  GetChatsRequest clone() => GetChatsRequest()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  GetChatsRequest copyWith(void Function(GetChatsRequest) updates) => super.copyWith((message) => updates(message as GetChatsRequest)) as GetChatsRequest;

  $pb.BuilderInfo get info_ => _i;

  @$core.pragma('dart2js:noInline')
  static GetChatsRequest create() => GetChatsRequest._();
  GetChatsRequest createEmptyInstance() => create();
  static $pb.PbList<GetChatsRequest> createRepeated() => $pb.PbList<GetChatsRequest>();
  @$core.pragma('dart2js:noInline')
  static GetChatsRequest getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<GetChatsRequest>(create);
  static GetChatsRequest? _defaultInstance;
}

class GetChatsResponse extends $pb.GeneratedMessage {
  factory GetChatsResponse({
    $core.String? id,
    $core.String? subject,
    $core.int? messageCount,
    $core.int? participantCount,
    $4.Timestamp? lastMessage,
  }) {
    final $result = create();
    if (id != null) {
      $result.id = id;
    }
    if (subject != null) {
      $result.subject = subject;
    }
    if (messageCount != null) {
      $result.messageCount = messageCount;
    }
    if (participantCount != null) {
      $result.participantCount = participantCount;
    }
    if (lastMessage != null) {
      $result.lastMessage = lastMessage;
    }
    return $result;
  }
  GetChatsResponse._() : super();
  factory GetChatsResponse.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory GetChatsResponse.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);

  static final $pb.BuilderInfo _i = $pb.BuilderInfo(_omitMessageNames ? '' : 'GetChatsResponse', package: const $pb.PackageName(_omitMessageNames ? '' : 'dissipate.v1'), createEmptyInstance: create)
    ..aOS(1, _omitFieldNames ? '' : 'id')
    ..aOS(2, _omitFieldNames ? '' : 'subject')
    ..a<$core.int>(3, _omitFieldNames ? '' : 'messageCount', $pb.PbFieldType.O3)
    ..a<$core.int>(4, _omitFieldNames ? '' : 'participantCount', $pb.PbFieldType.O3)
    ..aOM<$4.Timestamp>(5, _omitFieldNames ? '' : 'lastMessage', subBuilder: $4.Timestamp.create)
    ..hasRequiredFields = false
  ;

  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  GetChatsResponse clone() => GetChatsResponse()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  GetChatsResponse copyWith(void Function(GetChatsResponse) updates) => super.copyWith((message) => updates(message as GetChatsResponse)) as GetChatsResponse;

  $pb.BuilderInfo get info_ => _i;

  @$core.pragma('dart2js:noInline')
  static GetChatsResponse create() => GetChatsResponse._();
  GetChatsResponse createEmptyInstance() => create();
  static $pb.PbList<GetChatsResponse> createRepeated() => $pb.PbList<GetChatsResponse>();
  @$core.pragma('dart2js:noInline')
  static GetChatsResponse getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<GetChatsResponse>(create);
  static GetChatsResponse? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get id => $_getSZ(0);
  @$pb.TagNumber(1)
  set id($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasId() => $_has(0);
  @$pb.TagNumber(1)
  void clearId() => clearField(1);

  @$pb.TagNumber(2)
  $core.String get subject => $_getSZ(1);
  @$pb.TagNumber(2)
  set subject($core.String v) { $_setString(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasSubject() => $_has(1);
  @$pb.TagNumber(2)
  void clearSubject() => clearField(2);

  @$pb.TagNumber(3)
  $core.int get messageCount => $_getIZ(2);
  @$pb.TagNumber(3)
  set messageCount($core.int v) { $_setSignedInt32(2, v); }
  @$pb.TagNumber(3)
  $core.bool hasMessageCount() => $_has(2);
  @$pb.TagNumber(3)
  void clearMessageCount() => clearField(3);

  @$pb.TagNumber(4)
  $core.int get participantCount => $_getIZ(3);
  @$pb.TagNumber(4)
  set participantCount($core.int v) { $_setSignedInt32(3, v); }
  @$pb.TagNumber(4)
  $core.bool hasParticipantCount() => $_has(3);
  @$pb.TagNumber(4)
  void clearParticipantCount() => clearField(4);

  @$pb.TagNumber(5)
  $4.Timestamp get lastMessage => $_getN(4);
  @$pb.TagNumber(5)
  set lastMessage($4.Timestamp v) { setField(5, v); }
  @$pb.TagNumber(5)
  $core.bool hasLastMessage() => $_has(4);
  @$pb.TagNumber(5)
  void clearLastMessage() => clearField(5);
  @$pb.TagNumber(5)
  $4.Timestamp ensureLastMessage() => $_ensure(4);
}


const _omitFieldNames = $core.bool.fromEnvironment('protobuf.omit_field_names');
const _omitMessageNames = $core.bool.fromEnvironment('protobuf.omit_message_names');
