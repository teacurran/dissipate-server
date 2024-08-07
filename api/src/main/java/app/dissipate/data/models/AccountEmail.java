package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "account_emails")
public class AccountEmail extends DefaultPanacheEntityWithTimestamps {
    @ManyToOne
    public Account account;

    public String email;

    public Instant validated;

    public boolean primary;

  public void setEmail(String email){
    this.email = email == null ? null : email.toLowerCase();
  }

}
