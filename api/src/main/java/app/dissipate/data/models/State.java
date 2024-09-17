package app.dissipate.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.geolatte.geom.C2D;
import org.geolatte.geom.Point;

import java.util.List;

@Entity
@Table(name = "states")
public class State extends DefaultPanacheEntityWithTimestamps {

  public String name;
  public String stateCode;
  public Point<C2D> location;
  public String type;

  @ManyToOne
  public Country country;

  @OneToMany
  public List<City> cities;
}
