//
//  Generated code. Do not modify.
//  source: dissipate/v1/chat.proto
//
// @dart = 2.12

// ignore_for_file: annotate_overrides, camel_case_types, comment_references
// ignore_for_file: constant_identifier_names, library_prefixes
// ignore_for_file: non_constant_identifier_names, prefer_final_fields
// ignore_for_file: unnecessary_import, unnecessary_this, unused_import

import 'dart:convert' as $convert;
import 'dart:core' as $core;
import 'dart:typed_data' as $typed_data;

@$core.Deprecated('Use getChatsRequestDescriptor instead')
const GetChatsRequest$json = {
  '1': 'GetChatsRequest',
};

/// Descriptor for `GetChatsRequest`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List getChatsRequestDescriptor = $convert.base64Decode(
    'Cg9HZXRDaGF0c1JlcXVlc3Q=');

@$core.Deprecated('Use getChatsResponseDescriptor instead')
const GetChatsResponse$json = {
  '1': 'GetChatsResponse',
  '2': [
    {'1': 'id', '3': 1, '4': 1, '5': 9, '10': 'id'},
    {'1': 'subject', '3': 2, '4': 1, '5': 9, '10': 'subject'},
    {'1': 'message_count', '3': 3, '4': 1, '5': 5, '10': 'messageCount'},
    {'1': 'participant_count', '3': 4, '4': 1, '5': 5, '10': 'participantCount'},
    {'1': 'last_message', '3': 5, '4': 1, '5': 11, '6': '.google.protobuf.Timestamp', '10': 'lastMessage'},
  ],
};

/// Descriptor for `GetChatsResponse`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List getChatsResponseDescriptor = $convert.base64Decode(
    'ChBHZXRDaGF0c1Jlc3BvbnNlEg4KAmlkGAEgASgJUgJpZBIYCgdzdWJqZWN0GAIgASgJUgdzdW'
    'JqZWN0EiMKDW1lc3NhZ2VfY291bnQYAyABKAVSDG1lc3NhZ2VDb3VudBIrChFwYXJ0aWNpcGFu'
    'dF9jb3VudBgEIAEoBVIQcGFydGljaXBhbnRDb3VudBI9CgxsYXN0X21lc3NhZ2UYBSABKAsyGi'
    '5nb29nbGUucHJvdG9idWYuVGltZXN0YW1wUgtsYXN0TWVzc2FnZQ==');

