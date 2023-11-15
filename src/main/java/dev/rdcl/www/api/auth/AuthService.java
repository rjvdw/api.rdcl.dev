package dev.rdcl.www.api.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.rdcl.www.api.auth.dto.AuthenticatorAssertionResult;
import dev.rdcl.www.api.auth.dto.InitiateLoginResult;
import dev.rdcl.www.api.auth.dto.LoginMode;
import dev.rdcl.www.api.auth.entities.AllowedCallback;
import dev.rdcl.www.api.auth.entities.Authenticator;
import dev.rdcl.www.api.auth.entities.Identity;
import dev.rdcl.www.api.auth.entities.LoginAttempt;
import dev.rdcl.www.api.auth.errors.CredentialJsonException;
import dev.rdcl.www.api.auth.errors.InvalidCallback;
import dev.rdcl.www.api.auth.errors.LoginAttemptNotFound;
import dev.rdcl.www.api.auth.errors.UserNotFound;
import dev.rdcl.www.api.auth.events.InitiateLoginAttemptCompleteEvent;
import dev.rdcl.www.api.auth.events.InitiateLoginAttemptEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.enterprise.event.Reception;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;

import java.net.URI;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

@ApplicationScoped
@RequiredArgsConstructor
public class AuthService {

    private final AuthProperties authProperties;

    private final EntityManager em;

    private final AuthenticatorService authenticatorService;

    private final AuthMailService authMailService;

    private final Event<InitiateLoginAttemptEvent> initiateLoginAttemptEvent;

    private final Event<InitiateLoginAttemptCompleteEvent> initiateLoginAttemptCompleteEvent;

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

    public Identity getUser(String email) {
        try {
            return em
                    .createNamedQuery("Identity.findByEmail", Identity.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new UserNotFound(e);
        }
    }

    @Transactional
    public Identity updateUser(UUID id, Consumer<Identity> updater) {
        Identity identity = getUser(id);
        updater.accept(identity);
        em.persist(identity);
        em.flush();
        return identity;
    }

    @Transactional
    public InitiateLoginResult initiateLogin(String email, URI callback, LoginMode mode) {
        validateCallback(callback);

        List<Authenticator> authenticators = authenticatorService.getAuthenticators(email);

        if (authenticators.isEmpty() || LoginMode.EMAIL.equals(mode)) {
            String sessionToken = generateToken(authProperties.sessionTokenLength());

            initiateLoginAttemptEvent.fireAsync(new InitiateLoginAttemptEvent(
                    email,
                    callback,
                    sessionToken
            ));

            return new InitiateLoginResult(
                    LoginMode.EMAIL,
                    sessionToken,
                    null
            );
        }

        try {
            AuthenticatorAssertionResult authenticatorAssertionResult = authenticatorService.initiateLogin(email);
            return new InitiateLoginResult(
                    LoginMode.AUTHENTICATOR,
                    authenticatorAssertionResult.options(),
                    authenticatorAssertionResult.id()
            );
        } catch (JsonProcessingException e) {
            throw new CredentialJsonException(e);
        }
    }

    @Transactional
    public Identity completeLogin(UUID assertionId, String credentialJson) throws JsonProcessingException {
        String email = authenticatorService.completeLogin(assertionId, credentialJson);

        return getUser(email);
    }

    @Transactional
    public CompletionStage<InitiateLoginAttemptCompleteEvent> consume(
            @ObservesAsync(notifyObserver = Reception.IF_EXISTS) InitiateLoginAttemptEvent event
    ) {
        try {
            Identity identity = em
                    .createNamedQuery("Identity.findByEmail", Identity.class)
                    .setParameter("email", event.email())
                    .getSingleResult();

            LoginAttempt loginAttempt = LoginAttempt.builder()
                    .sessionToken(event.sessionToken())
                    .verificationCode(generateToken(authProperties.verificationCodeLength()))
                    .identity(identity)
                    .build();

            em.persist(loginAttempt);
            em.flush();

            authMailService.sendVerificationMail(
                    event.email(),
                    loginAttempt.getVerificationCode(),
                    event.callback()
            );

            return initiateLoginAttemptCompleteEvent.fireAsync(InitiateLoginAttemptCompleteEvent.initiated());
        } catch (NoResultException e) {
            // ignore this login
            return initiateLoginAttemptCompleteEvent.fireAsync(InitiateLoginAttemptCompleteEvent.aborted());
        }
    }

    @Transactional
    public Identity verifyLogin(String sessionToken, String verificationCode) {
        try {
            LoginAttempt loginAttempt = em
                    .createNamedQuery("LoginAttempt.findBySessionTokenAndVerificationCode", LoginAttempt.class)
                    .setParameter("sessionToken", sessionToken)
                    .setParameter("verificationCode", verificationCode)
                    .setParameter("createdAfter", expiryThreshold())
                    .getSingleResult();

            em.remove(loginAttempt);

            return loginAttempt.getIdentity();
        } catch (NoResultException e) {
            throw new LoginAttemptNotFound(e);
        }
    }

    @Scheduled(every = "1h")
    @Transactional
    public void cleanUpOldLoginAttempts() {
        em.createNamedQuery("LoginAttempt.deleteExpired")
                .setParameter("createdBefore", expiryThreshold())
                .executeUpdate();
    }

    private Instant expiryThreshold() {
        return Instant.now().minusSeconds(authProperties.maxLoginAttemptDurationSeconds());
    }

    private void validateCallback(URI callback) {
        if (callback != null) {
            String url = callback.toString();
            em.createNamedQuery("AllowedCallback.findByUrl", AllowedCallback.class)
                    .setParameter("url", url)
                    .getResultStream()
                    .findFirst()
                    .orElseThrow(() -> new InvalidCallback(url));
        }
    }

    private String generateToken(int length) {
        SecureRandom random = new SecureRandom();
        byte[] token = new byte[length];
        random.nextBytes(token);
        return Base64.encodeBase64URLSafeString(token);
    }

}
