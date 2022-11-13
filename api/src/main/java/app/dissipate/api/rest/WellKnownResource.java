package app.dissipate.api.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/.well-known")
public class WellKnownResource {

    @GET
    @Path("/webfinger")
    @Produces(MediaType.APPLICATION_JSON)
    public String webfinger() {
        return "Hello from dissipate!";
    }
}
