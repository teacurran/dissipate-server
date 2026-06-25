//
//  Generated code. Do not modify.
//  source: dissipate/v1/common.proto
//
// @dart = 2.12

// ignore_for_file: annotate_overrides, camel_case_types, comment_references
// ignore_for_file: constant_identifier_names, library_prefixes
// ignore_for_file: non_constant_identifier_names, prefer_final_fields
// ignore_for_file: unnecessary_import, unnecessary_this, unused_import

import 'dart:convert' as $convert;
import 'dart:core' as $core;
import 'dart:typed_data' as $typed_data;

@$core.Deprecated('Use roleDescriptor instead')
const Role$json = {
  '1': 'Role',
  '2': [
    {'1': 'ROLE_UNSPECIFIED', '2': 0},
    {'1': 'ROLE_USER', '2': 1},
    {'1': 'ROLE_VERIFIED', '2': 2},
    {'1': 'ROLE_ADMIN', '2': 3},
  ],
};

/// Descriptor for `Role`. Decode as a `google.protobuf.EnumDescriptorProto`.
final $typed_data.Uint8List roleDescriptor = $convert.base64Decode(
    'CgRSb2xlEhQKEFJPTEVfVU5TUEVDSUZJRUQQABINCglST0xFX1VTRVIQARIRCg1ST0xFX1ZFUk'
    'lGSUVEEAISDgoKUk9MRV9BRE1JThAD');

@$core.Deprecated('Use accountStatusDescriptor instead')
const AccountStatus$json = {
  '1': 'AccountStatus',
  '2': [
    {'1': 'ACCOUNT_STATUS_UNSPECIFIED', '2': 0},
    {'1': 'ACCOUNT_STATUS_ANONYMOUS', '2': 1},
    {'1': 'ACCOUNT_STATUS_ACTIVE', '2': 2},
    {'1': 'ACCOUNT_STATUS_DISABLED', '2': 3},
    {'1': 'ACCOUNT_STATUS_SUSPENDED', '2': 4},
    {'1': 'ACCOUNT_STATUS_BANNED', '2': 5},
  ],
};

/// Descriptor for `AccountStatus`. Decode as a `google.protobuf.EnumDescriptorProto`.
final $typed_data.Uint8List accountStatusDescriptor = $convert.base64Decode(
    'Cg1BY2NvdW50U3RhdHVzEh4KGkFDQ09VTlRfU1RBVFVTX1VOU1BFQ0lGSUVEEAASHAoYQUNDT1'
    'VOVF9TVEFUVVNfQU5PTllNT1VTEAESGQoVQUNDT1VOVF9TVEFUVVNfQUNUSVZFEAISGwoXQUND'
    'T1VOVF9TVEFUVVNfRElTQUJMRUQQAxIcChhBQ0NPVU5UX1NUQVRVU19TVVNQRU5ERUQQBBIZCh'
    'VBQ0NPVU5UX1NUQVRVU19CQU5ORUQQBQ==');

@$core.Deprecated('Use methodPolicyDescriptor instead')
const MethodPolicy$json = {
  '1': 'MethodPolicy',
  '2': [
    {'1': 'allow_unauthenticated', '3': 1, '4': 1, '5': 8, '10': 'allowUnauthenticated'},
    {'1': 'allow_app', '3': 2, '4': 1, '5': 8, '10': 'allowApp'},
    {'1': 'scopes', '3': 3, '4': 3, '5': 9, '10': 'scopes'},
    {'1': 'min_role', '3': 4, '4': 1, '5': 14, '6': '.dissipate.v1.Role', '10': 'minRole'},
    {'1': 'cost', '3': 5, '4': 1, '5': 13, '10': 'cost'},
  ],
};

/// Descriptor for `MethodPolicy`. Decode as a `google.protobuf.DescriptorProto`.
final $typed_data.Uint8List methodPolicyDescriptor = $convert.base64Decode(
    'CgxNZXRob2RQb2xpY3kSMwoVYWxsb3dfdW5hdXRoZW50aWNhdGVkGAEgASgIUhRhbGxvd1VuYX'
    'V0aGVudGljYXRlZBIbCglhbGxvd19hcHAYAiABKAhSCGFsbG93QXBwEhYKBnNjb3BlcxgDIAMo'
    'CVIGc2NvcGVzEi0KCG1pbl9yb2xlGAQgASgOMhIuZGlzc2lwYXRlLnYxLlJvbGVSB21pblJvbG'
    'USEgoEY29zdBgFIAEoDVIEY29zdA==');

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

