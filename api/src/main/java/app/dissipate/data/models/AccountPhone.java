package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "account_phones")
public class AccountPhone extends DefaultPanacheEntityWithTimestamps {
    @ManyToOne
    public Account account;

    public String phone;

    private Instant validated;

    public boolean isPrimary;
}
