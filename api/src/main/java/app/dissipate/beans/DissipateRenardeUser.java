package app.dissipate.beans;

import java.util.Set;

public class DissipateRenardeUser {

  private String userId;
  private Set<String> roles;
  private boolean registered;

  public Set<String> roles() {
    return roles;
  }

  public String userId() {
    return userId;
  }

  public boolean registered() {
    return registered;
  }
}
