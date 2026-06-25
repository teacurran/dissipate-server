package app.dissipate.api.grpc;

import app.dissipate.grpc.v1.ChatService;
import app.dissipate.grpc.v1.GetChatsRequest;
import app.dissipate.grpc.v1.GetChatsResponse;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import app.dissipate.interceptors.GrpcSecurityInterceptor;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;

@GrpcService
@RegisterInterceptor(GrpcSecurityInterceptor.class)
@RegisterInterceptor(GrpcLocaleInterceptor.class)
public class ChatServiceImpl implements ChatService {

  @Override
  public Multi<GetChatsResponse> getChats(GetChatsRequest request) {
    // TODO(phase-4): implement chat listing. Stubbed as an empty stream for now.
    return Multi.createFrom().empty();
  }
}
