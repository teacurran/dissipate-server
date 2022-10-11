package app.dissipate.data.models;

import app.dissipate.data.models.dto.MaxIntDto;
import io.smallrye.mutiny.Uni;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.LocalDateTime;

@Entity
public class Server extends DefaultPanacheEntityWithTimestamps {

    public int instanceNumber;

    public LocalDateTime seen;

    @Column(nullable = false, columnDefinition="BOOLEAN DEFAULT false")
    public boolean isShutdown;

    public static Uni<MaxIntDto> findMaxInstanceId() {
        return find("select coalesce(max(instanceNumber), 0) as maxValue from Server").project(MaxIntDto.class).firstResult();
    }

    public static Uni<Server> findFirstUnusedServer() {
        return find("isShutdown=true").firstResult();
    }

    public static Uni<Server> byId(Long id) {
        return findById(id);
    }

}
