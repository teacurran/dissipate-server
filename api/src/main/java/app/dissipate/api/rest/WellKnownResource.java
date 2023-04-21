package app.dissipate.api.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/.well-known")
public class WellKnownResource {

    @GET
    @Path("/webfinger")
    @Produces(MediaType.APPLICATION_JSON)
    public String webfinger() {
        return "Hello from dissipate!";
    }
}
