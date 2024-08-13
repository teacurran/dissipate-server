package app.dissipate.data.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.annotations.ColumnTransformer;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Identity extends DefaultPanacheEntityWithTimestamps {
  @ConfigProperty(name = "encryption.key")
  @Transient
  String key;

  public String name;
  public String publicKey;

  @ColumnTransformer(
    read = "PGP_SYM_DECRYPT(privateKey, '${encryption.key}')",
    write = "PGP_SYM_ENCRYPT(?, '${encryption.key}')")
  @Column(columnDefinition = "bytea")
  public String privateKey;

  @ManyToOne
  public Account account;

  @OneToMany(
    mappedBy = "identity",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  public List<IdentityOrganization> organizations = new ArrayList<IdentityOrganization>();

  @OneToMany(
    mappedBy = "approvedBy",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  public List<IdentityOrganization> membershipApprovals = new ArrayList<IdentityOrganization>();

  public String language;

  @OneToMany(
    mappedBy = "identity",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  List<IdentityFollow> following = new ArrayList<IdentityFollow>();

  @OneToMany(
    mappedBy = "identity2",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  List<IdentityFollow> followers = new ArrayList<IdentityFollow>();
}
