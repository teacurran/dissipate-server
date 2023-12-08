package app.dissipate.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import io.opentelemetry.api.trace.Span;
import io.quarkus.runtime.StartupEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import java.io.IOException;

@ApplicationScoped
public class FirebaseService {

    private static FirebaseApp firebaseApp;

    public void onStart(@Observes StartupEvent event) {
        getFirebaseApp();
    }


    @Produces
    public FirebaseAuth getFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    //@WithSpan("get-firebase-app")
    public static FirebaseApp getFirebaseApp() {
        if (firebaseApp == null) {
            try {
                firebaseApp = FirebaseApp.getInstance();
                Span.current().addEvent("FirebaseApp reused after reload");
            } catch (IllegalStateException ise) {
                try {
                    Span.current().addEvent("FirebaseApp not initialized");
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.getApplicationDefault())
                            .build();

                    firebaseApp = FirebaseApp.initializeApp(options);
                } catch (IOException e) {
                    Span.current().recordException(e);
                }
            }
        }
        return firebaseApp;
    }


}
