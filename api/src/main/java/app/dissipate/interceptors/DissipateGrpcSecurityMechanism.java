package app.dissipate.interceptors;

import app.dissipate.constants.AuthenticationConstants;
import app.dissipate.data.models.Session;
import io.grpc.Metadata;
import io.quarkus.grpc.auth.GrpcSecurityMechanism;
import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import jakarta.enterprise.context.ApplicationScoped;

import static app.dissipate.constants.ApplicationConstants.MAX_DB_WAIT_DURATION;
import static app.dissipate.constants.ApplicationConstants.MAX_DB_WAIT_TIME;

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

    // Session session = Session.findBySidValidated(authString).await().atMost(MAX_DB_WAIT_DURATION);

    return new TokenAuthenticationRequest(new TokenCredential(authString, null));
  }
}
