package dev.rdcl.www.api.health.entity;

import dev.rdcl.www.api.auth.entities.Identity;
import dev.rdcl.www.api.restconfig.validators.Json;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "HealthSettings")
@Table(name = "health_settings")
@NamedQueries({
    @NamedQuery(name = "HealthSettings.get", query = """
        select hs
        from HealthSettings hs
        where hs.owner.id = :owner
        """),
})
public class HealthSettings {

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
    @Column(name = "settings", columnDefinition = "text")
    private String settings;
}
