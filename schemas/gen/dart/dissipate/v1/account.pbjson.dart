//
//  Generated code. Do not modify.
//  source: dissipate/v1/account.proto
//
// @dart = 2.12

// ignore_for_file: annotate_overrides, camel_case_types, comment_references
// ignore_for_file: constant_identifier_names, library_prefixes
// ignore_for_file: non_constant_identifier_names, prefer_final_fields
// ignore_for_file: unnecessary_import, unnecessary_this, unused_import

import 'dart:convert' as $convert;
import 'dart:core' as $core;
import 'dart:typed_data' as $typed_data;

@$core.Deprecated('Use registerResponseResultDescriptor instead')
const RegisterResponseResult$json = {
  '1': 'RegisterResponseResult',
  '2': [
    {'1': 'REGISTER_RESPONSE_RESULT_UNSPECIFIED', '2': 0},
    {'1': 'REGISTER_RESPONSE_RESULT_SESSION_CREATED', '2': 1},
    {'1': 'REGISTER_RESPONSE_RESULT_EMAIL_SENT', '2': 2},
    {'1': 'REGISTER_RESPONSE_RESULT_PHONE_SENT', '2': 3},
  ],
};

/// Descriptor for `RegisterResponseResult`. Decode as a `google.protobuf.EnumDescriptorProto`.
final $typed_data.Uint8List registerResponseResultDescriptor = $convert.base64Decode(
    'ChZSZWdpc3RlclJlc3BvbnNlUmVzdWx0EigKJFJFR0lTVEVSX1JFU1BPTlNFX1JFU1VMVF9VTl'
    'NQRUNJRklFRBAAEiwKKFJFR0lTVEVSX1JFU1BPTlNFX1JFU1VMVF9TRVNTSU9OX0NSRUFURUQQ'
    'ARInCiNSRUdJU1RFUl9SRVNQT05TRV9SRVNVTFRfRU1BSUxfU0VOVBACEicKI1JFR0lTVEVSX1'
    'JFU1BPTlNFX1JFU1VMVF9QSE9ORV9TRU5UEAM=');

@$core.Deprecated('Use registerRequestDescriptor instead')
const RegisterRequest$json = {
  '1': 'RegisterRequest',
  '2': [
    {'1': 'email', '3': 1, '4': 1, '5': 9, '9': 0, '10': 'email', '17': true},
    {'1': 'phone_number', '3': 2, '4': 1, '5': 9, '9': 1, '10': 'phoneNumber', '17': true},
  ],
  '8': [
    {'1': '_email'},
    {'1': '_phone_number'},
  ],
};

/// Descriptor for `RegisterRequest`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List registerRequestDescriptor = $convert.base64Decode(
    'Cg9SZWdpc3RlclJlcXVlc3QSGQoFZW1haWwYASABKAlIAFIFZW1haWyIAQESJgoMcGhvbmVfbn'
    'VtYmVyGAIgASgJSAFSC3Bob25lTnVtYmVyiAEBQggKBl9lbWFpbEIPCg1fcGhvbmVfbnVtYmVy');

@$core.Deprecated('Use registerResponseDescriptor instead')
const RegisterResponse$json = {
  '1': 'RegisterResponse',
  '2': [
    {'1': 'result', '3': 1, '4': 1, '5': 14, '6': '.dissipate.v1.RegisterResponseResult', '10': 'result'},
    {'1': 'sid', '3': 2, '4': 1, '5': 9, '9': 0, '10': 'sid', '17': true},
  ],
  '8': [
    {'1': '_sid'},
  ],
};

/// Descriptor for `RegisterResponse`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List registerResponseDescriptor = $convert.base64Decode(
    'ChBSZWdpc3RlclJlc3BvbnNlEjwKBnJlc3VsdBgBIAEoDjIkLmRpc3NpcGF0ZS52MS5SZWdpc3'
    'RlclJlc3BvbnNlUmVzdWx0UgZyZXN1bHQSFQoDc2lkGAIgASgJSABSA3NpZIgBAUIGCgRfc2lk');

@$core.Deprecated('Use validateSessionRequestDescriptor instead')
const ValidateSessionRequest$json = {
  '1': 'ValidateSessionRequest',
  '2': [
    {'1': 'sid', '3': 1, '4': 1, '5': 9, '10': 'sid'},
    {'1': 'otp', '3': 2, '4': 1, '5': 9, '10': 'otp'},
  ],
};

/// Descriptor for `ValidateSessionRequest`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List validateSessionRequestDescriptor = $convert.base64Decode(
    'ChZWYWxpZGF0ZVNlc3Npb25SZXF1ZXN0EhAKA3NpZBgBIAEoCVIDc2lkEhAKA290cBgCIAEoCV'
    'IDb3Rw');

@$core.Deprecated('Use validateSessionResponseDescriptor instead')
const ValidateSessionResponse$json = {
  '1': 'ValidateSessionResponse',
  '2': [
    {'1': 'valid', '3': 1, '4': 1, '5': 8, '10': 'valid'},
    {'1': 'error_message', '3': 2, '4': 1, '5': 9, '9': 0, '10': 'errorMessage', '17': true},
  ],
  '8': [
    {'1': '_error_message'},
  ],
};

/// Descriptor for `ValidateSessionResponse`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List validateSessionResponseDescriptor = $convert.base64Decode(
    'ChdWYWxpZGF0ZVNlc3Npb25SZXNwb25zZRIUCgV2YWxpZBgBIAEoCFIFdmFsaWQSKAoNZXJyb3'
    'JfbWVzc2FnZRgCIAEoCUgAUgxlcnJvck1lc3NhZ2WIAQFCEAoOX2Vycm9yX21lc3NhZ2U=');

