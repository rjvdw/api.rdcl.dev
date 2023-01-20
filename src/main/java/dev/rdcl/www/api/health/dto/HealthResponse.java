package dev.rdcl.www.api.health.dto;

import dev.rdcl.www.api.health.entity.Health;

import java.time.LocalDate;

public record HealthResponse(LocalDate date, String data) {
    public static HealthResponse from(Health health) {
        return new HealthResponse(health.getDate(), health.getData());
    }
}
