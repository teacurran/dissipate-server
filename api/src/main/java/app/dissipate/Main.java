package app.dissipate;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.jboss.logging.Logger;

import java.io.IOException;

@QuarkusMain
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class);

    private static FirebaseApp firebaseApp;

    public static void main(String ... args) {
        getFirebaseApp();
        Quarkus.run(args);
    }

    public static FirebaseApp getFirebaseApp() {
        if (firebaseApp == null) {
            try {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .build();

                firebaseApp = FirebaseApp.initializeApp(options);
            } catch (IOException e) {
                LOG.error("Failed to initialize firebase", e);
            }
        }
        return firebaseApp;
    }
}
