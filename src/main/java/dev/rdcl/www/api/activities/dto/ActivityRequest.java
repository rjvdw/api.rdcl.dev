package dev.rdcl.www.api.activities.dto;

import dev.rdcl.www.api.activities.entities.Activity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class ActivityRequest {

    @FormParam("title")
    @NotNull
    @Size(max = 511)
    private String title;

    @FormParam("description")
    private String description;

    @FormParam("notes")
    private String notes;

    @FormParam("url")
    @Size(max = 511)
    private String url;

    @FormParam("location")
    @NotNull
    @Size(max = 511)
    private String location;

    @FormParam("starts")
    @NotNull
    // FIXME: Validate date
    private String starts;

    @FormParam("ends")
    // FIXME: Validate date
    private String ends;

    @FormParam("all-day")
    @DefaultValue("false")
    private boolean allDay;

    public Activity toActivity() {
        return Activity.builder()
            .title(getTitle())
            .description(getDescription())
            .notes(getNotes())
            .url(getUrl())
            .location(getLocation())
            .starts(parseZonedDateTime(getStarts()))
            .ends(parseZonedDateTime(getEnds()))
            .allDay(isAllDay())
            .build();
    }

    private ZonedDateTime parseZonedDateTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return null;
        }

        return ZonedDateTime.parse(dateTime);
    }
}
