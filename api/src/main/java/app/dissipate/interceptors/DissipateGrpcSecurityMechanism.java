package app.dissipate.interceptors;

import app.dissipate.constants.AuthenticationConstants;
import io.grpc.Metadata;
import io.quarkus.grpc.auth.GrpcSecurityMechanism;
import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DissipateGrpcSecurityMechanism implements GrpcSecurityMechanism {

  @Override
  public boolean handles(Metadata metadata) {
    String authString = metadata.get(AuthenticationConstants.AUTH_HEADER_KEY);
    return authString != null;
  }

  @Override
  public AuthenticationRequest createAuthenticationRequest(Metadata metadata) {
    String authString = metadata.get(AuthenticationConstants.AUTH_HEADER_KEY);
    return new TokenAuthenticationRequest(new TokenCredential(authString, null));
  }
}
