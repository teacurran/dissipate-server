package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "product_price_varitions")
public class ProductPriceVarition extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  public Product product;

  @ManyToOne
  public Country country;

  public BigDecimal price;
}
