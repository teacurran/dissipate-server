package app.dissipate.services;

import app.dissipate.beans.AuthTokenVO;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuthenticationService {


    @WithSpan("verify-id-token")
    public AuthTokenVO verifyIdToken(String idToken) {
//        try {
            if ("test-auth-token".equals(idToken)) {
                Span.current().addEvent("Verified Firebase Token", Attributes.of(SemanticAttributes.ENDUSER_ID, "test-uid"));
                AuthTokenVO fbTokenVO = new AuthTokenVO();
                fbTokenVO.setUid("test-uid");
                fbTokenVO.setEmail("test@example.com");

                return fbTokenVO;
            }

        throw new RuntimeException("Invalid token");
//            FirebaseToken fbToken = firebaseAuth.verifyIdToken(idToken);
//            String uid = fbToken.getUid();
//
//            Span.current().addEvent("Verified Firebase Token", Attributes.of(SemanticAttributes.ENDUSER_ID, uid));
//            // span event is working, I'm not sure if baggage is. look into it.
//
//            // todo: figure out how to close the results of makeCurrent()
//            // Baggage.current().toBuilder().put(SemanticAttributes.ENDUSER_ID.getKey(), uid).build().storeInContext(Context.current()).makeCurrent();
//
//            return new FirebaseTokenVO(fbToken);
//        } catch (FirebaseAuthException e) {
//            throw new RuntimeException(e);
//        }
    }
}
