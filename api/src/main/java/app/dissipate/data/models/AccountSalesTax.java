package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "account_sales_taxes")
public class AccountSalesTax extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  public Account account;
}
