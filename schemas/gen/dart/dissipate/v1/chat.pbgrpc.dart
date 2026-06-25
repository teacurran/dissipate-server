//
//  Generated code. Do not modify.
//  source: dissipate/v1/chat.proto
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

import 'chat.pb.dart' as $1;

export 'chat.pb.dart';

@$pb.GrpcServiceName('dissipate.v1.ChatService')
class ChatServiceClient extends $grpc.Client {
  static final _$getChats = $grpc.ClientMethod<$1.GetChatsRequest, $1.GetChatsResponse>(
      '/dissipate.v1.ChatService/GetChats',
      ($1.GetChatsRequest value) => value.writeToBuffer(),
      ($core.List<$core.int> value) => $1.GetChatsResponse.fromBuffer(value));

  ChatServiceClient($grpc.ClientChannel channel,
      {$grpc.CallOptions? options,
      $core.Iterable<$grpc.ClientInterceptor>? interceptors})
      : super(channel, options: options,
        interceptors: interceptors);

  $grpc.ResponseStream<$1.GetChatsResponse> getChats($1.GetChatsRequest request, {$grpc.CallOptions? options}) {
    return $createStreamingCall(_$getChats, $async.Stream.fromIterable([request]), options: options);
  }
}

@$pb.GrpcServiceName('dissipate.v1.ChatService')
abstract class ChatServiceBase extends $grpc.Service {
  $core.String get $name => 'dissipate.v1.ChatService';

  ChatServiceBase() {
    $addMethod($grpc.ServiceMethod<$1.GetChatsRequest, $1.GetChatsResponse>(
        'GetChats',
        getChats_Pre,
        false,
        true,
        ($core.List<$core.int> value) => $1.GetChatsRequest.fromBuffer(value),
        ($1.GetChatsResponse value) => value.writeToBuffer()));
  }

  $async.Stream<$1.GetChatsResponse> getChats_Pre($grpc.ServiceCall call, $async.Future<$1.GetChatsRequest> request) async* {
    yield* getChats(call, await request);
  }

  $async.Stream<$1.GetChatsResponse> getChats($grpc.ServiceCall call, $1.GetChatsRequest request);
}
