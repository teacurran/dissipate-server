package app.dissipate.data.models;

import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "account_phones")
@NamedQuery(name = AccountPhone.QUERY_FIND_BY_VALIDATED_PHONE,
  query = """
    FROM AccountPhone
    WHERE phone = :phone
    AND validated IS NOT NULL
    """)
public class AccountPhone extends DefaultPanacheEntityWithTimestamps {

    public static final String QUERY_FIND_BY_VALIDATED_PHONE = "AccountPhone.findByValidatedPhone";

    @ManyToOne
    public Account account;

    public String phone;

    public Instant validated;

    public boolean isPrimary;

    /**
     * Lookup of the (at most one) validated owner of the given phone number.
     * Backed by the partial unique index {@code uidx_account_phones_validated},
     * so it returns the single account that has completed OTP validation for
     * this phone, or {@code null} if none. Callers should normalize the phone
     * number to E.164 before invoking.
     */
    public static Uni<AccountPhone> findByValidatedPhone(String phone) {
        return find("#" + AccountPhone.QUERY_FIND_BY_VALIDATED_PHONE, Parameters.with("phone", phone)).firstResult();
    }
}
