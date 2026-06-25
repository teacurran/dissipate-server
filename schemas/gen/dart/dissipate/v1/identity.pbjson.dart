//
//  Generated code. Do not modify.
//  source: dissipate/v1/identity.proto
//
// @dart = 2.12

// ignore_for_file: annotate_overrides, camel_case_types, comment_references
// ignore_for_file: constant_identifier_names, library_prefixes
// ignore_for_file: non_constant_identifier_names, prefer_final_fields
// ignore_for_file: unnecessary_import, unnecessary_this, unused_import

import 'dart:convert' as $convert;
import 'dart:core' as $core;
import 'dart:typed_data' as $typed_data;

@$core.Deprecated('Use createIdentityRequestDescriptor instead')
const CreateIdentityRequest$json = {
  '1': 'CreateIdentityRequest',
  '2': [
    {'1': 'username', '3': 1, '4': 1, '5': 9, '10': 'username'},
    {'1': 'name', '3': 2, '4': 1, '5': 9, '10': 'name'},
  ],
};

/// Descriptor for `CreateIdentityRequest`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List createIdentityRequestDescriptor = $convert.base64Decode(
    'ChVDcmVhdGVJZGVudGl0eVJlcXVlc3QSGgoIdXNlcm5hbWUYASABKAlSCHVzZXJuYW1lEhIKBG'
    '5hbWUYAiABKAlSBG5hbWU=');

@$core.Deprecated('Use createIdentityResponseDescriptor instead')
const CreateIdentityResponse$json = {
  '1': 'CreateIdentityResponse',
  '2': [
    {'1': 'sid', '3': 1, '4': 1, '5': 9, '10': 'sid'},
    {'1': 'iid', '3': 2, '4': 1, '5': 9, '10': 'iid'},
    {'1': 'username', '3': 3, '4': 1, '5': 9, '10': 'username'},
    {'1': 'name', '3': 4, '4': 1, '5': 9, '10': 'name'},
    {'1': 'created', '3': 5, '4': 1, '5': 11, '6': '.google.protobuf.Timestamp', '10': 'created'},
  ],
};

/// Descriptor for `CreateIdentityResponse`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List createIdentityResponseDescriptor = $convert.base64Decode(
    'ChZDcmVhdGVJZGVudGl0eVJlc3BvbnNlEhAKA3NpZBgBIAEoCVIDc2lkEhAKA2lpZBgCIAEoCV'
    'IDaWlkEhoKCHVzZXJuYW1lGAMgASgJUgh1c2VybmFtZRISCgRuYW1lGAQgASgJUgRuYW1lEjQK'
    'B2NyZWF0ZWQYBSABKAsyGi5nb29nbGUucHJvdG9idWYuVGltZXN0YW1wUgdjcmVhdGVk');

@$core.Deprecated('Use changeIdentityRequestDescriptor instead')
const ChangeIdentityRequest$json = {
  '1': 'ChangeIdentityRequest',
  '2': [
    {'1': 'iid', '3': 1, '4': 1, '5': 9, '10': 'iid'},
  ],
};

/// Descriptor for `ChangeIdentityRequest`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List changeIdentityRequestDescriptor = $convert.base64Decode(
    'ChVDaGFuZ2VJZGVudGl0eVJlcXVlc3QSEAoDaWlkGAEgASgJUgNpaWQ=');

@$core.Deprecated('Use changeIdentityResponseDescriptor instead')
const ChangeIdentityResponse$json = {
  '1': 'ChangeIdentityResponse',
  '2': [
    {'1': 'sid', '3': 1, '4': 1, '5': 9, '10': 'sid'},
    {'1': 'iid', '3': 2, '4': 1, '5': 9, '10': 'iid'},
    {'1': 'username', '3': 3, '4': 1, '5': 9, '10': 'username'},
    {'1': 'name', '3': 4, '4': 1, '5': 9, '10': 'name'},
    {'1': 'created', '3': 5, '4': 1, '5': 11, '6': '.google.protobuf.Timestamp', '10': 'created'},
  ],
};

/// Descriptor for `ChangeIdentityResponse`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List changeIdentityResponseDescriptor = $convert.base64Decode(
    'ChZDaGFuZ2VJZGVudGl0eVJlc3BvbnNlEhAKA3NpZBgBIAEoCVIDc2lkEhAKA2lpZBgCIAEoCV'
    'IDaWlkEhoKCHVzZXJuYW1lGAMgASgJUgh1c2VybmFtZRISCgRuYW1lGAQgASgJUgRuYW1lEjQK'
    'B2NyZWF0ZWQYBSABKAsyGi5nb29nbGUucHJvdG9idWYuVGltZXN0YW1wUgdjcmVhdGVk');

