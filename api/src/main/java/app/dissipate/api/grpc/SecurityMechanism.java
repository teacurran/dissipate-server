package app.dissipate.api.grpc;

import app.dissipate.constants.AuthenticationConstants;
import app.dissipate.services.AuthenticationService;
import com.google.common.base.Strings;
import io.grpc.Metadata;
import io.quarkus.grpc.auth.GrpcSecurityMechanism;
import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.request.AuthenticationRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SecurityMechanism implements GrpcSecurityMechanism {

    @Inject
    AuthenticationService authenticationService;

    @Override
    public boolean handles(Metadata metadata) {
        String token = getToken(metadata);

        return !Strings.isNullOrEmpty(token);
    }

    private String getToken(Metadata metadata) {
        return metadata.get(AuthenticationConstants.AUTH_HEADER_KEY);
    }

    @Override
    public AuthenticationRequest createAuthenticationRequest(Metadata metadata) {
        String token = getToken(metadata);
        return new GrpcAuthenticationRequest(new TokenCredential(token, "grpc"));
    }
}