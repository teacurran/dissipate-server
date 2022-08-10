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

    public static void main(String ... args) {
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            LOG.error("Failed to initialize firebase", e);
        }

        Quarkus.run(args);
    }
}
