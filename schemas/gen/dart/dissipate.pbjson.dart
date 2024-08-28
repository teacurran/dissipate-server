//
//  Generated code. Do not modify.
//  source: dissipate.proto
//
// @dart = 2.12

// ignore_for_file: annotate_overrides, camel_case_types, comment_references
// ignore_for_file: constant_identifier_names, library_prefixes
// ignore_for_file: non_constant_identifier_names, prefer_final_fields
// ignore_for_file: unnecessary_import, unnecessary_this, unused_import

import 'dart:convert' as $convert;
import 'dart:core' as $core;
import 'dart:typed_data' as $typed_data;

@$core.Deprecated('Use accountStatusDescriptor instead')
const AccountStatus$json = {
  '1': 'AccountStatus',
  '2': [
    {'1': 'ACCOUNT_STATUS_ACTIVE', '2': 0},
    {'1': 'ACCOUNT_STATUS_DISABLED', '2': 1},
    {'1': 'ACCOUNT_STATUS_SUSPENDED', '2': 2},
    {'1': 'ACCOUNT_STATUS_BANNED', '2': 3},
  ],
};

/// Descriptor for `AccountStatus`. Decode as a `google.protobuf.EnumDescriptorProto`.
final $typed_data.Uint8List accountStatusDescriptor = $convert.base64Decode(
    'Cg1BY2NvdW50U3RhdHVzEhkKFUFDQ09VTlRfU1RBVFVTX0FDVElWRRAAEhsKF0FDQ09VTlRfU1'
    'RBVFVTX0RJU0FCTEVEEAESHAoYQUNDT1VOVF9TVEFUVVNfU1VTUEVOREVEEAISGQoVQUNDT1VO'
    'VF9TVEFUVVNfQkFOTkVEEAM=');

@$core.Deprecated('Use registerResponseResultDescriptor instead')
const RegisterResponseResult$json = {
  '1': 'RegisterResponseResult',
  '2': [
    {'1': 'REGISTER_RESPONSE_RESULT_ERROR_UNSPECIFIED', '2': 0},
    {'1': 'REGISTER_RESPONSE_RESULT_EMAIL_SENT', '2': 1},
    {'1': 'REGISTER_RESPONSE_RESULT_PHONE_SENT', '2': 2},
  ],
};

/// Descriptor for `RegisterResponseResult`. Decode as a `google.protobuf.EnumDescriptorProto`.
final $typed_data.Uint8List registerResponseResultDescriptor = $convert.base64Decode(
    'ChZSZWdpc3RlclJlc3BvbnNlUmVzdWx0Ei4KKlJFR0lTVEVSX1JFU1BPTlNFX1JFU1VMVF9FUl'
    'JPUl9VTlNQRUNJRklFRBAAEicKI1JFR0lTVEVSX1JFU1BPTlNFX1JFU1VMVF9FTUFJTF9TRU5U'
    'EAESJwojUkVHSVNURVJfUkVTUE9OU0VfUkVTVUxUX1BIT05FX1NFTlQQAg==');

@$core.Deprecated('Use createHandleRequestDescriptor instead')
const CreateHandleRequest$json = {
  '1': 'CreateHandleRequest',
  '2': [
    {'1': 'handle', '3': 1, '4': 1, '5': 9, '10': 'handle'},
  ],
};

/// Descriptor for `CreateHandleRequest`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List createHandleRequestDescriptor = $convert.base64Decode(
    'ChNDcmVhdGVIYW5kbGVSZXF1ZXN0EhYKBmhhbmRsZRgBIAEoCVIGaGFuZGxl');

@$core.Deprecated('Use createHandleResponseDescriptor instead')
const CreateHandleResponse$json = {
  '1': 'CreateHandleResponse',
  '2': [
    {'1': 'id', '3': 1, '4': 1, '5': 9, '10': 'id'},
    {'1': 'handle', '3': 2, '4': 1, '5': 9, '10': 'handle'},
    {'1': 'created', '3': 3, '4': 1, '5': 11, '6': '.google.protobuf.Timestamp', '10': 'created'},
    {'1': 'last_seen', '3': 4, '4': 1, '5': 11, '6': '.google.protobuf.Timestamp', '10': 'lastSeen'},
  ],
};

/// Descriptor for `CreateHandleResponse`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List createHandleResponseDescriptor = $convert.base64Decode(
    'ChRDcmVhdGVIYW5kbGVSZXNwb25zZRIOCgJpZBgBIAEoCVICaWQSFgoGaGFuZGxlGAIgASgJUg'
    'ZoYW5kbGUSNAoHY3JlYXRlZBgDIAEoCzIaLmdvb2dsZS5wcm90b2J1Zi5UaW1lc3RhbXBSB2Ny'
    'ZWF0ZWQSNwoJbGFzdF9zZWVuGAQgASgLMhouZ29vZ2xlLnByb3RvYnVmLlRpbWVzdGFtcFIIbG'
    'FzdFNlZW4=');

@$core.Deprecated('Use registerRequestDescriptor instead')
const RegisterRequest$json = {
  '1': 'RegisterRequest',
  '2': [
    {'1': 'locale', '3': 1, '4': 1, '5': 9, '9': 0, '10': 'locale', '17': true},
    {'1': 'email', '3': 2, '4': 1, '5': 9, '9': 1, '10': 'email', '17': true},
    {'1': 'phone_number', '3': 3, '4': 1, '5': 9, '9': 2, '10': 'phoneNumber', '17': true},
  ],
  '8': [
    {'1': '_locale'},
    {'1': '_email'},
    {'1': '_phone_number'},
  ],
};

/// Descriptor for `RegisterRequest`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List registerRequestDescriptor = $convert.base64Decode(
    'Cg9SZWdpc3RlclJlcXVlc3QSGwoGbG9jYWxlGAEgASgJSABSBmxvY2FsZYgBARIZCgVlbWFpbB'
    'gCIAEoCUgBUgVlbWFpbIgBARImCgxwaG9uZV9udW1iZXIYAyABKAlIAlILcGhvbmVOdW1iZXKI'
    'AQFCCQoHX2xvY2FsZUIICgZfZW1haWxCDwoNX3Bob25lX251bWJlcg==');

@$core.Deprecated('Use apiErrorDescriptor instead')
const ApiError$json = {
  '1': 'ApiError',
  '2': [
    {'1': 'code', '3': 1, '4': 1, '5': 9, '10': 'code'},
    {'1': 'message', '3': 2, '4': 1, '5': 9, '10': 'message'},
  ],
};

/// Descriptor for `ApiError`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List apiErrorDescriptor = $convert.base64Decode(
    'CghBcGlFcnJvchISCgRjb2RlGAEgASgJUgRjb2RlEhgKB21lc3NhZ2UYAiABKAlSB21lc3NhZ2'
    'U=');

@$core.Deprecated('Use registerResponseDescriptor instead')
const RegisterResponse$json = {
  '1': 'RegisterResponse',
  '2': [
    {'1': 'result', '3': 1, '4': 1, '5': 14, '6': '.RegisterResponseResult', '10': 'result'},
    {'1': 'sid', '3': 2, '4': 1, '5': 9, '9': 0, '10': 'sid', '17': true},
    {'1': 'error', '3': 3, '4': 1, '5': 11, '6': '.ApiError', '9': 1, '10': 'error', '17': true},
  ],
  '8': [
    {'1': '_sid'},
    {'1': '_error'},
  ],
};

/// Descriptor for `RegisterResponse`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List registerResponseDescriptor = $convert.base64Decode(
    'ChBSZWdpc3RlclJlc3BvbnNlEi8KBnJlc3VsdBgBIAEoDjIXLlJlZ2lzdGVyUmVzcG9uc2VSZX'
    'N1bHRSBnJlc3VsdBIVCgNzaWQYAiABKAlIAFIDc2lkiAEBEiQKBWVycm9yGAMgASgLMgkuQXBp'
    'RXJyb3JIAVIFZXJyb3KIAQFCBgoEX3NpZEIICgZfZXJyb3I=');

@$core.Deprecated('Use validateSessionRequestDescriptor instead')
const ValidateSessionRequest$json = {
  '1': 'ValidateSessionRequest',
  '2': [
    {'1': 'locale', '3': 1, '4': 1, '5': 9, '9': 0, '10': 'locale', '17': true},
    {'1': 'sid', '3': 2, '4': 1, '5': 9, '8': {}, '10': 'sid'},
    {'1': 'otp', '3': 3, '4': 1, '5': 9, '8': {}, '10': 'otp'},
  ],
  '8': [
    {'1': '_locale'},
  ],
};

/// Descriptor for `ValidateSessionRequest`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List validateSessionRequestDescriptor = $convert.base64Decode(
    'ChZWYWxpZGF0ZVNlc3Npb25SZXF1ZXN0EhsKBmxvY2FsZRgBIAEoCUgAUgZsb2NhbGWIAQESrQ'
    'EKA3NpZBgCIAEoCUKaAbpIlgG6AZIBCgNzaWQaigF0aGlzLm1hdGNoZXMoJ15bMC05YS1mQS1G'
    'XXs4fS1bMC05YS1mQS1GXXs0fS1bMC05YS1mQS1GXXs0fS1bMC05YS1mQS1GXXs0fS1bMC05YS'
    '1mQS1GXXsxMn0kJykgPyBudWxsOiAnVmFsaWRhdGVTZXNzaW9uUmVxdWVzdC5zaWQuaW52YWxp'
    'ZCdSA3NpZBJsCgNvdHAYAyABKAlCWrpIV7oBVAoDb3RwGk10aGlzLm1hdGNoZXMoJ15bMC05QS'
    '1aYS16XXs2fSQnKSA/IG51bGw6ICdWYWxpZGF0ZVNlc3Npb25SZXF1ZXN0Lm90cC5pbnZhbGlk'
    'J1IDb3RwQgkKB19sb2NhbGU=');

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

