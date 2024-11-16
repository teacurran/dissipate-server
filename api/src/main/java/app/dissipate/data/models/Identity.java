package app.dissipate.data.models;

import app.dissipate.utils.EncryptionUtil;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Entity
@Table(name = "identities", indexes = {
  @Index(name = "uidx_identity_username_normalized", columnList = "usernameNormalized", unique = true)
})
@NamedQuery(name = Identity.QUERY_BY_USERNAME, query = """
  SELECT i
  FROM Identity i
  WHERE i.usernameNormalized = :username
  """)
public class Identity extends DefaultPanacheEntityWithTimestamps {

  public static final String ID_GENERATOR_KEY = "Identity";

  public static final String QUERY_BY_USERNAME = "Identity.findByUsername";

  public String username;

  public String usernameNormalized;

  public String name;

  public String publicKey;

  @Transient
  public String privateKey;

  @Transient
  public String privateKeyKey;

  public byte[] privateKeyEncrypted;

  @ManyToOne
  public Account account;

  public String timezone;

  public Locale locale;

  @OneToMany(
    mappedBy = "identity",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  public List<IdentityOrganization> organizations = new ArrayList<>();

  @OneToMany(
    mappedBy = "approvedBy",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  public List<IdentityOrganization> membershipApprovals = new ArrayList<>();

  @OneToMany(
    mappedBy = "identity",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  List<IdentityFollow> following = new ArrayList<>();

  @OneToMany(
    mappedBy = "identity2",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  List<IdentityFollow> followers = new ArrayList<>();

  public static Uni<Identity> findById(Object id) {
    return Identity.findById(id);
  }

  public Uni<Identity> persistAndFlush(EncryptionUtil encryptionUtil) {
    encryptFields(encryptionUtil);
    return super.persistAndFlush();
  }

  @WithSpan
  public void encryptFields(EncryptionUtil eu) {
    if (this.privateKey != null) {
      this.privateKeyEncrypted = eu.encrypt(this.privateKey, this.privateKeyKey);
      this.privateKey = null;
    }
  }

}
