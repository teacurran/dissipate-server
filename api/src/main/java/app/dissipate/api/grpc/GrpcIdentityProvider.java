package app.dissipate.api.grpc;

import app.dissipate.beans.AuthTokenVO;
import app.dissipate.services.AuthenticationService;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.credential.Credential;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.security.Permission;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class GrpcIdentityProvider implements IdentityProvider<GrpcAuthenticationRequest> {
    private static final Logger LOGGER = Logger.getLogger(GrpcIdentityProvider.class);

    private static SecurityIdentity ACCESSOR_ROLE = null;

    @Inject
    AuthenticationService authenticationService;

    public GrpcIdentityProvider() {
        // default constructor required for injection
    }

    @Override
    public Class<GrpcAuthenticationRequest> getRequestType() {
        return GrpcAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(GrpcAuthenticationRequest request, AuthenticationRequestContext context) {
        LOGGER.info("authenticate request:" + request);

        AuthTokenVO fbToken = authenticationService.verifyIdToken(request.getToken().getToken());

        if (fbToken == null) {
            //return Uni.createFrom().item(anonymous(context));
            throw new AuthenticationFailedException();
        }
        return Uni.createFrom().item(QuarkusSecurityIdentity.builder()
                .setPrincipal(fbToken::getUid)
                        .addAttribute("fb_token", fbToken)
                .addRole(GrpcRole.grpc()).build());
    }

    private SecurityIdentity anonymous(AuthenticationRequestContext context) {
        return anonInstance;
    }

    private static final Principal principal = () -> "";

    private static final SecurityIdentity anonInstance = new SecurityIdentity() {
        @Override
        public Principal getPrincipal() {
            return principal;
        }

        @Override
        public boolean isAnonymous() {
            return true;
        }

        @Override
        public Set<String> getRoles() {
            return Collections.emptySet();
        }

        @Override
        public boolean hasRole(String role) {
            return false;
        }

        @Override
        public <T extends Credential> T getCredential(Class<T> credentialType) {
            return null;
        }

        @Override
        public Set<Credential> getCredentials() {
            return Collections.emptySet();
        }

        @Override
        public <T> T getAttribute(String name) {
            return null;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return Collections.emptyMap();
        }

        @Override
        public Uni<Boolean> checkPermission(Permission permission) {
            return Uni.createFrom().item(false);
        }
    };
}