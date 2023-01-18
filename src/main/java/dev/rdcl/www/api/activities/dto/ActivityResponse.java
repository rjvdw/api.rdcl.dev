package dev.rdcl.www.api.activities.dto;

import dev.rdcl.www.api.activities.entities.Activity;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public record ActivityResponse(
    UUID id,
    String title,
    String description,
    String notes,
    String url,
    String location,
    String starts,
    String ends,
    boolean allDay,
    List<String> labels
) {
    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(
            activity.getId(),
            activity.getTitle(),
            activity.getDescription(),
            activity.getNotes(),
            activity.getUrl(),
            activity.getLocation(),
            nullableToString(activity.getStarts()),
            nullableToString(activity.getEnds()),
            activity.isAllDay(),
            activity.getLabels()
        );
    }

    private static String nullableToString(ZonedDateTime zdt) {
        if (zdt == null) {
            return null;
        }

        return zdt.toOffsetDateTime().toString();
    }
}
