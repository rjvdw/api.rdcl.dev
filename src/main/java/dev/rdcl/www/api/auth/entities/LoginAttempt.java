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
@Entity(name = "LoginAttempt")
@Table(name = "auth_login_attempt")
@NamedQueries({
    @NamedQuery(name = "LoginAttempt.findBySessionTokenAndVerificationCode", query = """
        select l
        from LoginAttempt l
        where l.sessionToken = :sessionToken
        and l.verificationCode = :verificationCode
        and l.created > :createdAfter
        """),
    @NamedQuery(name = "LoginAttempt.deleteExpired", query = """
        delete from LoginAttempt l
        where l.created < :createdBefore
        """),
})
public class LoginAttempt {

    final private static int SESSION_TOKEN_SIZE = 64;
    final private static int VERIFICATION_CODE_SIZE = 64;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, insertable = false, updatable = false)
    @ColumnDefault("gen_random_uuid()")
    private UUID id;

    @Column(name = "session_token", nullable = false, updatable = false)
    private String sessionToken;

    @Column(name = "verification_code", nullable = false, updatable = false)
    private String verificationCode;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "identity", nullable = false, updatable = false)
    private Identity identity;

    @Column(name = "created", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant created;
}
