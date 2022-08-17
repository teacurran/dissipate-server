package app.dissipate.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class AuthenticationService {

    public String verifyIdToken(String idToken) {
        try {
            FirebaseToken fbToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            return fbToken.getUid();
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }
    }
}
