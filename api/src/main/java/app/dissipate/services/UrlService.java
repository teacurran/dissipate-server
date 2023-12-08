package app.dissipate.services;

import app.dissipate.data.models.Url;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Duration;
import java.time.Instant;

@ApplicationScoped
public class UrlService {

    @Channel("url-created-out")
    Emitter<Url> urlCreatedEmitter;

    @Inject
    Tracer tracer;

    @WithSession
    @WithTransaction
    public Uni<Url> addUrl(final String value) {
        Url url = new Url();
        url.url = value;
        return url.persistAndFlush().chain(() -> {
            urlCreatedEmitter.send(url);
            return Uni.createFrom().item(url);
        });
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
