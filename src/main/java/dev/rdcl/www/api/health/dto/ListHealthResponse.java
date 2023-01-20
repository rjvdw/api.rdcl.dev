package dev.rdcl.www.api.health.dto;

import dev.rdcl.www.api.health.entity.Health;

import java.util.List;

public record ListHealthResponse(List<HealthResponse> health) {

    public static ListHealthResponse from(List<Health> health) {
        return new ListHealthResponse(health.stream()
            .map(HealthResponse::from)
            .toList());
    }
}
