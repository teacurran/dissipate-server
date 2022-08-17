package app.dissipate.models;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.annotations.ColumnTransformer;

import javax.persistence.*;

@Entity
public class Handle extends DefaultPanacheEntity {
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


}
