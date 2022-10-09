package app.dissipate.data.models.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class MaxDto {
    public final long max;

    public MaxDto(long max) {
        this.max = max;
    }
}
