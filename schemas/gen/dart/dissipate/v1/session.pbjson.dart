//
//  Generated code. Do not modify.
//  source: dissipate/v1/session.proto
//
// @dart = 2.12

// ignore_for_file: annotate_overrides, camel_case_types, comment_references
// ignore_for_file: constant_identifier_names, library_prefixes
// ignore_for_file: non_constant_identifier_names, prefer_final_fields
// ignore_for_file: unnecessary_import, unnecessary_this, unused_import

import 'dart:convert' as $convert;
import 'dart:core' as $core;
import 'dart:typed_data' as $typed_data;

@$core.Deprecated('Use getSessionRequestDescriptor instead')
const GetSessionRequest$json = {
  '1': 'GetSessionRequest',
};

/// Descriptor for `GetSessionRequest`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List getSessionRequestDescriptor = $convert.base64Decode(
    'ChFHZXRTZXNzaW9uUmVxdWVzdA==');

@$core.Deprecated('Use getSessionResponseDescriptor instead')
const GetSessionResponse$json = {
  '1': 'GetSessionResponse',
  '2': [
    {'1': 'sid', '3': 1, '4': 1, '5': 9, '10': 'sid'},
    {'1': 'iid', '3': 2, '4': 1, '5': 9, '10': 'iid'},
    {'1': 'status', '3': 3, '4': 1, '5': 14, '6': '.dissipate.v1.AccountStatus', '10': 'status'},
    {'1': 'created', '3': 4, '4': 1, '5': 11, '6': '.google.protobuf.Timestamp', '10': 'created'},
    {'1': 'last_seen', '3': 5, '4': 1, '5': 11, '6': '.google.protobuf.Timestamp', '10': 'lastSeen'},
  ],
};

/// Descriptor for `GetSessionResponse`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List getSessionResponseDescriptor = $convert.base64Decode(
    'ChJHZXRTZXNzaW9uUmVzcG9uc2USEAoDc2lkGAEgASgJUgNzaWQSEAoDaWlkGAIgASgJUgNpaW'
    'QSMwoGc3RhdHVzGAMgASgOMhsuZGlzc2lwYXRlLnYxLkFjY291bnRTdGF0dXNSBnN0YXR1cxI0'
    'CgdjcmVhdGVkGAQgASgLMhouZ29vZ2xlLnByb3RvYnVmLlRpbWVzdGFtcFIHY3JlYXRlZBI3Cg'
    'lsYXN0X3NlZW4YBSABKAsyGi5nb29nbGUucHJvdG9idWYuVGltZXN0YW1wUghsYXN0U2Vlbg==');

