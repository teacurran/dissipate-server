package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "products")
public class Product extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  public Identity identity;

  @ManyToOne
  public Organization organization;

  public ProductType type;

  public BigDecimal price;

  @OneToMany(mappedBy = "product")
  public List<ProductPriceVarition> priceVaritions;

  public String name;

  public String url;

  public String summary;

  public String description;

}
