package app.dissipate.controllers;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Path;

public class Index {

  @CheckedTemplate
  public static class Templates {
    public static native TemplateInstance index();
  }

  @Path("/")
  public Uni<TemplateInstance> index() {
    return Uni.createFrom().item(Templates.index());
  }
}