package dev.rdcl.www.api.label.entities;

import dev.rdcl.www.api.auth.entities.Identity;
import dev.rdcl.www.api.restconfig.validators.Json;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Label")
@Table(name = "label", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"owner", "text"}),
})
@NamedQueries({
        @NamedQuery(name = "Label.find", query = """
                select l
                from Label l
                where l.owner.id = :owner
                """),

        @NamedQuery(name = "Label.clear", query = """
                delete from Label l
                where l.owner.id = :owner
                """),
})
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, insertable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner", nullable = false, updatable = false)
    private Identity owner;

    @Column(name = "text", nullable = false, length = 31)
    private String text;

    @Json
    @Column(name = "settings", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String settings;
}
