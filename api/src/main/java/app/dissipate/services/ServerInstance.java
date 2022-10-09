package app.dissipate.services;

import app.dissipate.data.models.Server;
import app.dissipate.data.models.dto.MaxDto;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class ServerInstance {

    Server server;

    public void onStart(@Observes @Initialized(ApplicationScoped.class) Object pointless) {
        MaxDto maxDto = Server.findMaxInstanceId().await().indefinitely();
        if (maxDto == null) {
            server = new Server();
            server.instanceNumber = 1;
        } else {
            if (maxDto.max < 1024) {
                server = new Server();
                server.instanceNumber = (int) maxDto.max + 1;
            } else {
                server = Server.findFirstUnusedServer().await().indefinitely();
            }
        }

        if (server == null) {
            throw new RuntimeException("No server instance available");
        }
        server.persistAndFlush();
    }

}
