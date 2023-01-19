package dev.rdcl.www.api.activities.entities;

import dev.rdcl.www.api.auth.entities.Identity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

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
        order by startsInstant
        """),

    @NamedQuery(name = "Activity.findPast", query = """
        select a
        from Activity a
        where a.owner.id = :owner
        and whenPivot < :when
        order by coalesce(a.endsInstant, a.startsInstant) desc
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner", nullable = false, updatable = false)
    private Identity owner;

    @Column(name = "title", nullable = false, length = 511)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Column(name = "url", length = 511)
    private String url;

    @Column(name = "location", nullable = false, length = 511)
    private String location;

    @Column(name = "timezone", nullable = false, length = 63)
    private String timezone;

    @Transient
    public ZoneId getZoneId() {
        return ZoneId.of(getTimezone());
    }

    @Transient
    public void setZoneId(ZoneId zoneId) {
        setTimezone(zoneId.toString());
    }

    @Column(name = "starts", nullable = false)
    private Instant startsInstant;

    @Transient
    public ZonedDateTime getStarts() {
        return asZonedDateTime(getStartsInstant());
    }

    @Transient
    public void setStarts(ZonedDateTime starts) {
        setStartsInstant(starts == null ? null : starts.toInstant());
    }

    @Column(name = "ends")
    private Instant endsInstant;

    @Column(name = "when_pivot", nullable = false, insertable = false, updatable = false)
    private Instant whenPivot;

    @Transient
    public ZonedDateTime getEnds() {
        return asZonedDateTime(getEndsInstant());
    }

    @Transient
    public void setEnds(ZonedDateTime ends) {
        setEndsInstant(ends == null ? null : ends.toInstant());
    }

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "all_day", nullable = false)
    @ColumnDefault("false")
    private boolean allDay;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "activity_label", joinColumns = {
        @JoinColumn(name = "activity"),
    })
    @Column(name = "text", nullable = false, updatable = false)
    private List<String> labels;

    private ZonedDateTime asZonedDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }

        ZoneId zoneId = getZoneId();
        return instant.atZone(zoneId);
    }
}
