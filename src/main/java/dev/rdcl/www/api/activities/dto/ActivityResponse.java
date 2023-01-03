package dev.rdcl.www.api.activities.dto;

import dev.rdcl.www.api.activities.entities.Activity;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public record ActivityResponse(
    UUID id,
    String title,
    String description,
    String notes,
    String url,
    String location,
    String starts,
    String ends,
    boolean allDay
) {
    public static ActivityResponse from(Activity activity) {
        DateTimeFormatter formatter = activity.isAllDay()
            ? ISO_LOCAL_DATE
            : ISO_OFFSET_DATE_TIME;

        return new ActivityResponse(
            activity.getId(),
            activity.getTitle(),
            activity.getDescription(),
            activity.getNotes(),
            activity.getUrl(),
            activity.getLocation(),
            activity.getStarts().format(formatter),
            activity.getEnds() == null ? null : activity.getEnds().format(formatter),
            activity.isAllDay()
        );
    }
}
