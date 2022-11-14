package app.dissipate.data.models;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Account extends PanacheEntityBase {
    @Id
    public Long id;

    @Column(unique = true)
    public String srcId;
    public String email;
    public String phone;
    public AccountStatus status;

    @OneToMany(
            mappedBy = "account",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    public List<Identity> identities = new ArrayList<>();

    public static Uni<Account> findBySrcId(String srcId) {
        return find("srcId", srcId).firstResult();
    }

    @Override
    public Uni<Account> persistAndFlush() {
        return super.persistAndFlush();
    }

    public void setEmail(String email){
        this.email = email.toLowerCase();
    }

    public enum AccountStatus {
        ACTIVE(1),
        DISABLED(2),
        SUSPENDED(3),
        BANNED(4);

        private int value;
        private AccountStatus(int value) {
            this.value = value;
        }

        public static AccountStatus fromValue(int id) {
            for (AccountStatus item : AccountStatus.values()) {
                if (item.getValue() == id) {
                    return item;
                }
            }
            return null;
        }

        public int getValue() {
            return value;
        }

    }
}

