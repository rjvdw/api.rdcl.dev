package dev.rdcl.www.api.activities.dto;

import dev.rdcl.www.api.activities.entities.Activity;

import java.time.ZonedDateTime;
import java.util.UUID;

public record ActivityResponse(
    UUID id,
    String title,
    String description,
    String notes,
    String url,
    String location,
    ZonedDateTime starts,
    ZonedDateTime ends,
    boolean allDay
) {
    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(
            activity.getId(),
            activity.getTitle(),
            activity.getDescription(),
            activity.getNotes(),
            activity.getUrl(),
            activity.getLocation(),
            activity.getStarts(),
            activity.getEnds(),
            activity.isAllDay()
        );
    }
}
