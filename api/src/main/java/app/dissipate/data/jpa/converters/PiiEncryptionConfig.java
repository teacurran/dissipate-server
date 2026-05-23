package app.dissipate.data.jpa.converters;

import app.dissipate.utils.EncryptionUtil;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Bridges Quarkus CDI/MicroProfile Config to the JPA {@link EncryptedStringConverter}, which is
 * instantiated by Hibernate and is not CDI-managed. We capture the configured key and a
 * {@link EncryptionUtil} instance into static fields at startup so converters can use them.
 *
 * <p>The {@code dissipate.pii.key} value should be a high-entropy secret provisioned via the
 * environment (Kubernetes Secret in prod). It is hashed with SHA-256 inside EncryptionUtil to
 * derive a 256-bit AES key, so any string length is accepted, but real deployments should use a
 * base64-encoded 32-byte random value.
 */
@Startup
@ApplicationScoped
public class PiiEncryptionConfig {

  @Inject
  EncryptionUtil encryptionUtil;

  @ConfigProperty(name = "dissipate.pii.key")
  String configuredKey;

  private static volatile String KEY;
  private static volatile EncryptionUtil EU;

  @PostConstruct
  void init() {
    KEY = configuredKey;
    EU = encryptionUtil;
  }

  static String key() {
    String k = KEY;
    if (k == null) {
      throw new IllegalStateException(
        "dissipate.pii.key is not configured; cannot encrypt/decrypt PII columns");
    }
    return k;
  }

  static EncryptionUtil encryptionUtil() {
    EncryptionUtil eu = EU;
    if (eu == null) {
      // Fallback for environments where the converter is exercised before CDI startup
      // (e.g. very early Hibernate bootstrap). Safe because EncryptionUtil is stateless.
      eu = new EncryptionUtil();
    }
    return eu;
  }
}
