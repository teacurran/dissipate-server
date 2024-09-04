package app.dissipate.data.models;

import app.dissipate.utils.EncryptionUtil;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Entity
@Table(name = "identities")
public class Identity extends DefaultPanacheEntityWithTimestamps {

  @ConfigProperty(name = "encryption.key")
  @Transient
  String key;

  public String name;

  public String publicKey;

  @Transient
  public String privateKey;

  @Transient
  public String privateKeyKey;

  public byte[] privateKeyEncrypted;

  @ManyToOne
  public Account account;

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

  public Locale locale;

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

  public Uni<Account> persistAndFlush(EncryptionUtil encryptionUtil) {
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
