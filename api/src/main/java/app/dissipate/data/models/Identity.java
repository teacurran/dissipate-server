package app.dissipate.data.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.annotations.ColumnTransformer;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Identity extends PanacheEntity {
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

    @ManyToOne(fetch = FetchType.LAZY)
    public Account account;

    @OneToMany(
            mappedBy = "identity",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    public List<Channel> channels = new ArrayList<>();

    @OneToMany(
            mappedBy = "identity",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    public List<Channel> posts = new ArrayList<>();

}
