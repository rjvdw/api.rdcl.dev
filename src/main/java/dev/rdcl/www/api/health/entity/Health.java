package dev.rdcl.www.api.health.entity;

import dev.rdcl.www.api.auth.entities.Identity;
import dev.rdcl.www.api.restconfig.validators.Json;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Health")
@IdClass(HealthKey.class)
@Table(name = "health")
@NamedQueries({
    @NamedQuery(name = "Health.findBefore", query = """
        select h
        from Health h
        where h.owner.id = :owner
        and h.date <= :to
        order by h.date desc
        """),

    @NamedQuery(name = "Health.findAfter", query = """
        select h
        from Health h
        where h.owner.id = :owner
        and h.date >= :from
        order by h.date desc
        """),

    @NamedQuery(name = "Health.findBetween", query = """
        select h
        from Health h
        where h.owner.id = :owner
        and h.date between :from and :to
        order by h.date desc
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

    @NamedQuery(name = "Health.countBefore", query = """
        select count(h.id.date)
        from Health h
        where h.owner.id = :owner
        and h.date <= :to
        """),

    @NamedQuery(name = "Health.countAfter", query = """
        select count(h.id.date)
        from Health h
        where h.owner.id = :owner
        and h.date >= :from
        """),

    @NamedQuery(name = "Health.countBetween", query = """
        select count(h.id.date)
        from Health h
        where h.owner.id = :owner
        and h.date between :from and :to
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
    @Column(name = "data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String data;

}
