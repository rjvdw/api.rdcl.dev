package dev.rdcl.www.api.settings.entity;

import dev.rdcl.www.api.auth.entities.Identity;
import dev.rdcl.www.api.restconfig.validators.Json;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "UserSettings")
@Table(name = "user_settings")
@NamedQueries({
        @NamedQuery(name = "UserSettings.get", query = """
                select us
                from UserSettings us
                where us.owner.id = :owner
                """),
})
public class UserSettings {

    @JsonbTransient
    @Id
    @Column(name = "owner", nullable = false, updatable = false)
    private UUID id;

    @JsonbTransient
    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner", nullable = false, updatable = false)
    private Identity owner;

    @Json
    @Column(name = "settings", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String settings;
}
