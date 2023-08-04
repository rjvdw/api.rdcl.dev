package dev.rdcl.www.api.auth;

import dev.rdcl.www.api.auth.entities.Identity;
import dev.rdcl.www.api.auth.entities.LoginAttempt;
import dev.rdcl.www.api.auth.errors.LoginAttemptNotFound;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class AuthServiceTest {

    @Inject
    AuthProperties authProperties;

    @Inject
    AuthService authService;

    @Inject
    EntityManager em;

    @Test
    @TestTransaction
    @DisplayName("Login attempts can be verified")
    public void testVerifyLogin() {
        Identity identity = Identity.builder()
            .name("Test Subject")
            .email("test.subject@example.com")
            .build();
        em.persist(identity);

        LoginAttempt loginAttempt = createLoginAttempt(identity, pastInstant(maxDuration() - 10));
        em.persist(loginAttempt);

        Identity verified = authService.verifyLogin(loginAttempt.getSessionToken(), loginAttempt.getVerificationCode());

        assertThat(identity.getId(), is(verified.getId()));
    }

    @Test
    @TestTransaction
    @DisplayName("Login attempts that are expired are ignored")
    public void testVerifyExpiredLogin() {
        Identity identity = Identity.builder()
            .name("Test Subject")
            .email("test.subject@example.com")
            .build();
        em.persist(identity);

        LoginAttempt loginAttempt = createLoginAttempt(identity, pastInstant(maxDuration() + 10));

        assertThrows(LoginAttemptNotFound.class, () -> {
            authService.verifyLogin(loginAttempt.getSessionToken(), loginAttempt.getVerificationCode());
        });
    }

    @Test
    @TestTransaction
    @DisplayName("Old login attempts are removed while recent login attempts are kept intact")
    public void testCleanUpOldLoginAttempts() {
        Identity identity = Identity.builder()
            .name("Test Subject")
            .email("test.subject@example.com")
            .build();

        em.persist(identity);

        // should be kept
        createLoginAttempt(identity, Instant.now());

        // should be kept
        createLoginAttempt(identity, pastInstant(maxDuration() - 10));

        // should be deleted
        createLoginAttempt(identity, pastInstant(maxDuration() + 10));

        authService.cleanUpOldLoginAttempts();

        List<LoginAttempt> loginAttempts = em
            .createQuery("""
                select l
                from LoginAttempt l
                where l.identity = :identity
                """, LoginAttempt.class)
            .setParameter("identity", identity)
            .getResultList();

        assertThat(loginAttempts, hasSize(2));
        for (LoginAttempt la : loginAttempts) {
            Duration age = Duration.between(la.getCreated(), Instant.now());
            assertTrue(age.getSeconds() < maxDuration());
        }
    }

    private int maxDuration() {
        return authProperties.maxLoginAttemptDurationSeconds();
    }

    private Instant pastInstant(long secondsInPast) {
        return Instant.now().minusSeconds(secondsInPast);
    }

    private LoginAttempt createLoginAttempt(Identity identity, Instant created) {
        LoginAttempt loginAttempt = LoginAttempt.builder()
            .sessionToken(UUID.randomUUID().toString())
            .verificationCode(UUID.randomUUID().toString())
            .identity(identity)
            .build();

        em.persist(loginAttempt);

        // override the creation timestamp
        em.createQuery("""
                update LoginAttempt l
                set l.created = :created
                where l = :loginAttempt
                """)
            .setParameter("created", created)
            .setParameter("loginAttempt", loginAttempt)
            .executeUpdate();

        return loginAttempt;
    }
}
