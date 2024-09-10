package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  Identity identity;
}
