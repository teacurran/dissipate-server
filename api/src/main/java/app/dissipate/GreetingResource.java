package app.dissipate;

import app.dissipate.services.UrlService;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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