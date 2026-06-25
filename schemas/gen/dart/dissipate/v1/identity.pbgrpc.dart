//
//  Generated code. Do not modify.
//  source: dissipate/v1/identity.proto
//
// @dart = 2.12

// ignore_for_file: annotate_overrides, camel_case_types, comment_references
// ignore_for_file: constant_identifier_names, library_prefixes
// ignore_for_file: non_constant_identifier_names, prefer_final_fields
// ignore_for_file: unnecessary_import, unnecessary_this, unused_import

import 'dart:async' as $async;
import 'dart:core' as $core;

import 'package:grpc/service_api.dart' as $grpc;
import 'package:protobuf/protobuf.dart' as $pb;

import 'identity.pb.dart' as $2;

export 'identity.pb.dart';

@$pb.GrpcServiceName('dissipate.v1.IdentityService')
class IdentityServiceClient extends $grpc.Client {
  static final _$createIdentity = $grpc.ClientMethod<$2.CreateIdentityRequest, $2.CreateIdentityResponse>(
      '/dissipate.v1.IdentityService/CreateIdentity',
      ($2.CreateIdentityRequest value) => value.writeToBuffer(),
      ($core.List<$core.int> value) => $2.CreateIdentityResponse.fromBuffer(value));
  static final _$changeIdentity = $grpc.ClientMethod<$2.ChangeIdentityRequest, $2.ChangeIdentityResponse>(
      '/dissipate.v1.IdentityService/ChangeIdentity',
      ($2.ChangeIdentityRequest value) => value.writeToBuffer(),
      ($core.List<$core.int> value) => $2.ChangeIdentityResponse.fromBuffer(value));

  IdentityServiceClient($grpc.ClientChannel channel,
      {$grpc.CallOptions? options,
      $core.Iterable<$grpc.ClientInterceptor>? interceptors})
      : super(channel, options: options,
        interceptors: interceptors);

  $grpc.ResponseFuture<$2.CreateIdentityResponse> createIdentity($2.CreateIdentityRequest request, {$grpc.CallOptions? options}) {
    return $createUnaryCall(_$createIdentity, request, options: options);
  }

  $grpc.ResponseFuture<$2.ChangeIdentityResponse> changeIdentity($2.ChangeIdentityRequest request, {$grpc.CallOptions? options}) {
    return $createUnaryCall(_$changeIdentity, request, options: options);
  }
}

@$pb.GrpcServiceName('dissipate.v1.IdentityService')
abstract class IdentityServiceBase extends $grpc.Service {
  $core.String get $name => 'dissipate.v1.IdentityService';

  IdentityServiceBase() {
    $addMethod($grpc.ServiceMethod<$2.CreateIdentityRequest, $2.CreateIdentityResponse>(
        'CreateIdentity',
        createIdentity_Pre,
        false,
        false,
        ($core.List<$core.int> value) => $2.CreateIdentityRequest.fromBuffer(value),
        ($2.CreateIdentityResponse value) => value.writeToBuffer()));
    $addMethod($grpc.ServiceMethod<$2.ChangeIdentityRequest, $2.ChangeIdentityResponse>(
        'ChangeIdentity',
        changeIdentity_Pre,
        false,
        false,
        ($core.List<$core.int> value) => $2.ChangeIdentityRequest.fromBuffer(value),
        ($2.ChangeIdentityResponse value) => value.writeToBuffer()));
  }

  $async.Future<$2.CreateIdentityResponse> createIdentity_Pre($grpc.ServiceCall call, $async.Future<$2.CreateIdentityRequest> request) async {
    return createIdentity(call, await request);
  }

  $async.Future<$2.ChangeIdentityResponse> changeIdentity_Pre($grpc.ServiceCall call, $async.Future<$2.ChangeIdentityRequest> request) async {
    return changeIdentity(call, await request);
  }

  $async.Future<$2.CreateIdentityResponse> createIdentity($grpc.ServiceCall call, $2.CreateIdentityRequest request);
  $async.Future<$2.ChangeIdentityResponse> changeIdentity($grpc.ServiceCall call, $2.ChangeIdentityRequest request);
}
