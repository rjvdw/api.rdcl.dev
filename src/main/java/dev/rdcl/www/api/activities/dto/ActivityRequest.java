package dev.rdcl.www.api.activities.dto;

import dev.rdcl.www.api.activities.entities.Activity;
import dev.rdcl.www.api.validators.IsoInstant;
import dev.rdcl.www.api.validators.IsoInstantValidator;
import dev.rdcl.www.api.validators.Timezone;
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

    @FormParam("timezone")
    @NotNull
    @Timezone
    private String timezone;

    @FormParam("starts")
    @NotNull
    @IsoInstant
    private String starts;

    @FormParam("ends")
    @IsoInstant
    private String ends;

    @FormParam("allDay")
    @DefaultValue("false")
    private boolean allDay;

    @FormParam("labels")
    private List<String> labels;

    public Activity toActivity() {
        return Activity.builder()
            .title(getTitle())
            .description(getDescription())
            .notes(getNotes())
            .url(getUrl())
            .location(getLocation())
            .timezone(getTimezone())
            .startsInstant(IsoInstantValidator.parse(getStarts()))
            .endsInstant(IsoInstantValidator.parse(getEnds()))
            .allDay(isAllDay())
            .labels(getLabels())
            .build();
    }
}
