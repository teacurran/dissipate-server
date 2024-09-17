package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.geolatte.geom.C2D;
import org.geolatte.geom.Point;

@Entity
@Table(name = "cities")
public class City extends DefaultPanacheEntityWithTimestamps {
  @ManyToOne
  public Country country;

  @ManyToOne
  public State state;

  public String name;

  public Point<C2D> location;
}
