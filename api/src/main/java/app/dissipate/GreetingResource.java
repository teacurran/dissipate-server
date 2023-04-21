package app.dissipate;

import app.dissipate.services.UrlService;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
@Path("/hello")
public class GreetingResource {

    @Inject
    UrlService urlService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        urlService.addUrl("http://grilledcheese.com");

        Span.current().addEvent("Added url", Attributes.of(
                AttributeKey.stringKey("url"), "http://grilledcheese.com"
        ));

        return "Hello from dissipate!";
    }
}