package app.dissipate.data.models;

import app.dissipate.data.models.dto.MaxDto;
import io.smallrye.mutiny.Uni;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.LocalDateTime;

@Entity
public class Server extends DefaultPanacheEntityWithTimestamps {

    public int instanceNumber;

    private LocalDateTime seen;

    @Column(nullable = true)
    private LocalDateTime shutdown;

    public static Uni<MaxDto> findMaxInstanceId() {
        return find("select max(instanceId)").project(MaxDto.class).firstResult();
    }

    public static Uni<Server> findFirstUnusedServer() {
        return find("shutdown IS NULL").firstResult();
    }

}
