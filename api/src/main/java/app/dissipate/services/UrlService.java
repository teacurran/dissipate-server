package app.dissipate.services;

import app.dissipate.data.cassandra.dao.UrlDao;
import app.dissipate.data.cassandra.models.Url;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.smallrye.common.annotation.Blocking;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Instant;

@ApplicationScoped
public class UrlService {

    @Channel("url-created-out")
    Emitter<Url> urlCreatedEmitter;

    @Inject
    UrlDao urlDao;

    @Inject
    Tracer tracer;

    public Url addUrl(final String value) {
        Url url = new Url(value);
        url.setDateCreated(Instant.now());
        urlDao.update(url);
        urlCreatedEmitter.send(url);

        return url;
    }

    @Incoming("url-created-in")
    @Blocking
    @Transactional
    public void urlAdded(Object message) {

        processMesasage(message);
    }

    public void processMesasage(Object message) {
        Span span = tracer.spanBuilder("process-message")
                .setAttribute(AttributeKey.stringKey("url"), message.toString())
                .setParent(Context.current().with(Span.current()))
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();
        span.addEvent("processing url", Attributes.of(
                AttributeKey.stringKey("url"), message.toString()
        ));
        span.end();
    }
}
