package app.dissipate.beans;

import io.quarkiverse.renarde.security.RenardeUser;

import java.util.Set;

public class DissipateRenardeUser implements RenardeUser {

  private String userId;
  private Set<String> roles;
  private boolean registered;

  @Override
  public Set<String> roles() {
    return roles;
  }

  @Override
  public String userId() {
    return userId;
  }

  @Override
  public boolean registered() {
    return registered;
  }
}
