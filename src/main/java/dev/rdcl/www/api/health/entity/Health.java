package dev.rdcl.www.api.health.entity;

import dev.rdcl.www.api.auth.entities.Identity;
import dev.rdcl.www.api.restconfig.validators.Json;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Health")
@IdClass(HealthKey.class)
@Table(name = "health")
@NamedQueries({
    @NamedQuery(name = "Health.findRecent", query = """
        select h
        from Health h
        where h.owner.id = :owner
        and h.date <= :now
        order by h.date desc
        """),

    @NamedQuery(name = "Health.findAfter", query = """
        select h
        from Health h
        where h.owner.id = :owner
        and h.date >= :from
        order by h.date
        """),

    @NamedQuery(name = "Health.findBetween", query = """
        select h
        from Health h
        where h.owner.id = :owner
        and h.date between :from and :to
        order by h.date
        """),

    @NamedQuery(name = "Health.findByDate", query = """
        select h
        from Health h
        where h.owner.id = :owner
        and h.date = :date
        """),

    @NamedQuery(name = "Health.count", query = """
        select count(h.id.date)
        from Health h
        where h.owner.id = :owner
        """),
})
public class Health {

    @Id
    @Column(name = "date", nullable = false, updatable = false)
    @ColumnDefault("now()")
    private LocalDate date;

    @JsonbTransient
    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner", nullable = false, updatable = false)
    private Identity owner;

    @Json
    @Column(name = "data", columnDefinition = "text")
    private String data;

}
