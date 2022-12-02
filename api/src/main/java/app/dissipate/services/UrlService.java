package app.dissipate.services;

import app.dissipate.data.cassandra.dao.UrlDao;
import app.dissipate.data.cassandra.models.Url;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
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
    public void urAdded(Object message) {

        System.out.println(message);
    }

}
