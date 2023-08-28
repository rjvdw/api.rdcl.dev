package dev.rdcl.www.api.auth.entities;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "AuthenticatorRegistration")
@Table(name = "auth_authenticator_registration")
@NamedQueries({
    @NamedQuery(name = "AuthenticatorRegistration.findByIdAndOwner", query = """
        select r
        from AuthenticatorRegistration r
        where r.id = :id and r.owner.id = :owner
        """),
})
public class AuthenticatorRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, insertable = false, updatable = false)
    @ColumnDefault("gen_random_uuid()")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner", nullable = false, updatable = false)
    private Identity owner;

    @Column(name = "options", updatable = false)
    private String options;

    @Column(name = "created", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant created;

    @Column(name = "timeout", nullable = false, updatable = false)
    private long timeout;
}
