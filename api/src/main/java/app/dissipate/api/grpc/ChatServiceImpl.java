package app.dissipate.api.grpc;

import app.dissipate.auth.PrincipalResolver;
import app.dissipate.grpc.v1.ChatService;
import app.dissipate.grpc.v1.GetChatsRequest;
import app.dissipate.grpc.v1.GetChatsResponse;
import app.dissipate.interceptors.GrpcLocaleInterceptor;
import app.dissipate.interceptors.GrpcSecurityInterceptor;
import io.quarkus.grpc.GrpcService;
import io.quarkus.grpc.RegisterInterceptor;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;

@GrpcService
@RegisterInterceptor(GrpcSecurityInterceptor.class)
@RegisterInterceptor(GrpcLocaleInterceptor.class)
public class ChatServiceImpl implements ChatService {

  @Inject
  PrincipalResolver principalResolver;

  // GetChats declares MethodPolicy(min_role: ROLE_USER). Gate the stream on authorize() so an
  // unauthorized caller gets a failed stream rather than an empty one. @WithSession can't annotate
  // a Multi-returning method, so open the reactive session explicitly for the authorize() lookup.
  @Override
  public Multi<GetChatsResponse> getChats(GetChatsRequest request) {
    return Panache.withSession(() -> principalResolver.authorize())
        .onItem().transformToMulti(principal ->
            // TODO(phase-4): implement chat listing. Stubbed as an empty stream for now.
            Multi.createFrom().empty());
  }
}
