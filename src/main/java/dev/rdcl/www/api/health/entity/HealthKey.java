package dev.rdcl.www.api.health.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class HealthKey implements Serializable {
    private LocalDate date;
    private UUID owner;
}
