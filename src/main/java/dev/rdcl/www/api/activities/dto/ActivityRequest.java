package dev.rdcl.www.api.activities.dto;

import dev.rdcl.www.api.activities.entities.Activity;
import dev.rdcl.www.api.validators.IsoDateTime;
import dev.rdcl.www.api.validators.IsoDateTimeValidator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import java.util.List;

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
    @IsoDateTime
    private String starts;

    @FormParam("ends")
    @IsoDateTime
    private String ends;

    @FormParam("allDay")
    @DefaultValue("false")
    private boolean allDay;

    @FormParam("labels")
    private List<String> labels;

    public Activity toActivity() {
        Activity activity = Activity.builder()
            .title(getTitle())
            .description(getDescription())
            .notes(getNotes())
            .url(getUrl())
            .location(getLocation())
            .allDay(isAllDay())
            .labels(getLabels())
            .build();

        activity.setStarts(IsoDateTimeValidator.parse(getStarts()));
        activity.setEnds(IsoDateTimeValidator.parse(getEnds()));

        return activity;
    }
}
