//
//  Generated code. Do not modify.
//  source: dissipate/v1/session.proto
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

import 'session.pb.dart' as $3;

export 'session.pb.dart';

@$pb.GrpcServiceName('dissipate.v1.SessionService')
class SessionServiceClient extends $grpc.Client {
  static final _$getSession = $grpc.ClientMethod<$3.GetSessionRequest, $3.GetSessionResponse>(
      '/dissipate.v1.SessionService/GetSession',
      ($3.GetSessionRequest value) => value.writeToBuffer(),
      ($core.List<$core.int> value) => $3.GetSessionResponse.fromBuffer(value));

  SessionServiceClient($grpc.ClientChannel channel,
      {$grpc.CallOptions? options,
      $core.Iterable<$grpc.ClientInterceptor>? interceptors})
      : super(channel, options: options,
        interceptors: interceptors);

  $grpc.ResponseFuture<$3.GetSessionResponse> getSession($3.GetSessionRequest request, {$grpc.CallOptions? options}) {
    return $createUnaryCall(_$getSession, request, options: options);
  }
}

@$pb.GrpcServiceName('dissipate.v1.SessionService')
abstract class SessionServiceBase extends $grpc.Service {
  $core.String get $name => 'dissipate.v1.SessionService';

  SessionServiceBase() {
    $addMethod($grpc.ServiceMethod<$3.GetSessionRequest, $3.GetSessionResponse>(
        'GetSession',
        getSession_Pre,
        false,
        false,
        ($core.List<$core.int> value) => $3.GetSessionRequest.fromBuffer(value),
        ($3.GetSessionResponse value) => value.writeToBuffer()));
  }

  $async.Future<$3.GetSessionResponse> getSession_Pre($grpc.ServiceCall call, $async.Future<$3.GetSessionRequest> request) async {
    return getSession(call, await request);
  }

  $async.Future<$3.GetSessionResponse> getSession($grpc.ServiceCall call, $3.GetSessionRequest request);
}
