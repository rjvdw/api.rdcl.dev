package dev.rdcl.www.api.auth;

import dev.rdcl.www.api.auth.entities.Identity;
import dev.rdcl.www.api.auth.entities.LoginAttempt;
import dev.rdcl.www.api.auth.errors.LoginAttemptNotFound;
import dev.rdcl.www.api.auth.errors.UserNotFound;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class AuthService {

    private final AuthProperties authProperties;

    private final EntityManager em;

    private final AuthMailService authMailService;

    public Identity getUser(UUID id) {
        try {
            return em
                .createNamedQuery("Identity.findById", Identity.class)
                .setParameter("id", id)
                .getSingleResult();
        } catch (NoResultException e) {
            throw new UserNotFound(e);
        }
    }

    @Transactional
    public String initiateLogin(String email) {
        String sessionToken = generateSessionToken();

        try {
            Identity identity = em
                .createNamedQuery("Identity.findByEmail", Identity.class)
                .setParameter("email", email)
                .getSingleResult();

            LoginAttempt loginAttempt = LoginAttempt.builder()
                .sessionToken(sessionToken)
                .verificationCode(generateVerificationCode())
                .identity(identity)
                .build();

            em.persist(loginAttempt);
            em.flush();

            authMailService.sendVerificationMail(identity.getEmail(), loginAttempt.getVerificationCode());
        } catch (NoResultException e) {
            // If the user does not exist, return a session token anyway.
        }

        return sessionToken;
    }

    @Transactional
    public Identity verifyLogin(String sessionToken, String verificationCode) {
        try {
            LoginAttempt loginAttempt = em
                .createNamedQuery("LoginAttempt.findBySessionTokenAndVerificationCode", LoginAttempt.class)
                .setParameter("sessionToken", sessionToken)
                .setParameter("verificationCode", verificationCode)
                .setParameter("createdAfter", Instant.now().minusSeconds(authProperties.maxLoginAttemptDurationSeconds()))
                .getSingleResult();

            em.remove(loginAttempt);

            return loginAttempt.getIdentity();
        } catch (NoResultException e) {
            throw new LoginAttemptNotFound(e);
        }
    }

    private String generateSessionToken() {
        SecureRandom random = new SecureRandom();
        byte[] sessionToken = new byte[authProperties.sessionTokenLength()];
        random.nextBytes(sessionToken);
        return encode(sessionToken);
    }

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        byte[] verificationCode = new byte[authProperties.verificationCodeLength()];
        random.nextBytes(verificationCode);
        return encode(verificationCode);
    }

    private static String encode(byte[] bytes) {
        return Base64.encodeBase64URLSafeString(bytes);
    }

}
