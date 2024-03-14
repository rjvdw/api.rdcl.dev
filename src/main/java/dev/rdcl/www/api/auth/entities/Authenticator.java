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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Authenticator")
@Table(name = "auth_authenticator")
@NamedQueries({
        @NamedQuery(name = "Authenticator.findByIdAndOwner", query = """
                select a
                from Authenticator a
                where a.owner.id = :owner and a.id = :id
                """),
        @NamedQuery(name = "Authenticator.findByEmail", query = """
                select a
                from Authenticator a
                where a.owner.email = :email
                """),
        @NamedQuery(name = "Authenticator.findByKeyId", query = """
                select a
                from Authenticator a
                join Identity i on a.owner = i
                where a.keyId = :keyId
                """),
})
public class Authenticator {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, insertable = false, updatable = false)
    @ColumnDefault("gen_random_uuid()")
    private UUID id;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner", nullable = false, updatable = false)
    private Identity owner;

    @Column(name = "key_id", nullable = false)
    private byte[] keyId;

    @Column(name = "cose", nullable = false)
    private byte[] cose;

    @Column(name = "signature_count", nullable = false)
    private Long signatureCount;

    @Column(name = "last_used")
    private Instant lastUsed;

}
