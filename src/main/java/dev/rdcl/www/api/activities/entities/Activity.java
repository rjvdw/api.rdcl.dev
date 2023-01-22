package dev.rdcl.www.api.activities.entities;

import dev.rdcl.www.api.auth.entities.Identity;
import dev.rdcl.www.api.restconfig.validators.Timezone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Activity")
@Table(name = "activity")
@NamedQueries({
    @NamedQuery(name = "Activity.findUpcoming", query = """
        select a
        from Activity a
        where a.owner.id = :owner
        and whenPivot >= :when
        order by starts
        """),

    @NamedQuery(name = "Activity.findPast", query = """
        select a
        from Activity a
        where a.owner.id = :owner
        and whenPivot < :when
        order by coalesce(a.ends, a.starts) desc
        """),

    @NamedQuery(name = "Activity.findById", query = """
        select a
        from Activity a
        where a.owner.id = :owner
        and a.id = :id
        """),
})
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, insertable = false, updatable = false)
    @ColumnDefault("gen_random_uuid()")
    private UUID id;

    @JsonbTransient
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner", nullable = false, updatable = false)
    private Identity owner;

    @FormParam("title")
    @NotNull
    @Size(max = 511)
    @Column(name = "title", nullable = false, length = 511)
    private String title;

    @FormParam("description")
    @Column(name = "description", columnDefinition = "text")
    private String description;

    @FormParam("notes")
    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @FormParam("url")
    @Size(max = 511)
    @Column(name = "url", length = 511)
    private String url;

    @FormParam("location")
    @NotNull
    @Size(max = 511)
    @Column(name = "location", nullable = false, length = 511)
    private String location;

    @FormParam("timezone")
    @NotNull
    @Timezone
    @Column(name = "timezone", nullable = false, length = 63)
    private String timezone;

    @JsonbTransient
    @Column(name = "starts", nullable = false)
    private Instant starts;

    @JsonbProperty("starts")
    @Transient
    public String getStartsSerialized() {
        return asIsoDateTimeString(getStarts());
    }

    @FormParam("starts")
    @Transient
    public void setStartsFromZoned(@NotNull ZonedDateTime starts) {
        setStarts(starts.toInstant());
    }

    @JsonbTransient
    @Column(name = "ends")
    private Instant ends;

    @JsonbTransient
    @Column(name = "when_pivot", nullable = false, insertable = false, updatable = false)
    private Instant whenPivot;

    @JsonbProperty("ends")
    @Transient
    public String getEndsSerialized() {
        return asIsoDateTimeString(getEnds());
    }

    @FormParam("ends")
    @Transient
    public void setEndsFromZoned(ZonedDateTime ends) {
        if (ends == null) {
            setEnds(null);
        } else {
            setEnds(ends.toInstant());
        }
    }

    @FormParam("allDay")
    @DefaultValue("false")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "all_day", nullable = false)
    @ColumnDefault("false")
    private boolean allDay;

    @FormParam("labels")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "activity_label", joinColumns = {
        @JoinColumn(name = "activity"),
    })
    @Column(name = "text", nullable = false, updatable = false)
    private List<String> labels;

    private String asIsoDateTimeString(Instant instant) {
        if (instant == null) {
            return null;
        }

        ZoneId zoneId = ZoneId.of(getTimezone());
        return instant.atZone(zoneId).toOffsetDateTime().toString();
    }
}
