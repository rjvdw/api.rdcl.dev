package dev.rdcl.www.api.activities.entities;

import dev.rdcl.www.api.auth.entities.Identity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
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
import java.time.ZonedDateTime;
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
        and (
            (a.allDay = true and date(coalesce(a.ends, a.starts)) >= date(:when)) or
            (a.allDay = false and (coalesce(a.ends, a.starts) >= :when))
        )
        order by starts
        """),

    @NamedQuery(name = "Activity.findPast", query = """
        select a
        from Activity a
        where a.owner.id = :owner
        and (
            (a.allDay = true and date(coalesce(a.ends, a.starts)) < date(:when)) or
            (a.allDay = false and (coalesce(a.ends, a.starts) < :when))
        )
        order by starts desc
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

    @Column(name = "starts", nullable = false)
    private ZonedDateTime starts;

    @Column(name = "ends")
    private ZonedDateTime ends;

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "all_day", nullable = false)
    @ColumnDefault("false")
    private boolean allDay;
}
