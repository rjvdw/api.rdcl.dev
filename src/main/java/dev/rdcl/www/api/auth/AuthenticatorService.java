package dev.rdcl.www.api.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserVerificationRequirement;
import com.yubico.webauthn.exception.RegistrationFailedException;
import dev.rdcl.www.api.auth.dto.RegisterResult;
import dev.rdcl.www.api.auth.entities.Authenticator;
import dev.rdcl.www.api.auth.entities.AuthenticatorRegistration;
import dev.rdcl.www.api.auth.entities.Identity;
import dev.rdcl.www.api.auth.errors.AuthenticatorNotFound;
import dev.rdcl.www.api.auth.errors.InvalidCredential;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Consumer;

@ApplicationScoped
@RequiredArgsConstructor
public class AuthenticatorService {

    private final AuthProperties authProperties;

    private final EntityManager em;

    private final AuthService authService;

    private final RelyingPartyService relyingPartyService;

    public Authenticator getAuthenticator(UUID owner, UUID authenticatorId) {
        Identity identity = authService.getUser(owner);

        try {
            return em
                .createNamedQuery("Authenticator.findByIdAndOwner", Authenticator.class)
                .setParameter("id", authenticatorId)
                .setParameter("owner", owner)
                .getSingleResult();
        } catch (NoResultException e) {
            throw new AuthenticatorNotFound(e);
        }
    }

    @Transactional
    public RegisterResult register(UUID owner) throws JsonProcessingException {
        Identity identity = authService.getUser(owner);

        Duration timeout = Duration.ofSeconds(authProperties.authenticatorTimeoutSeconds());

        PublicKeyCredentialCreationOptions options = relyingPartyService
            .getRelyingParty()
            .startRegistration(StartRegistrationOptions.builder()
                .user(Mappers.identityToUserIdentity(identity))
                .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                    .userVerification(UserVerificationRequirement.REQUIRED)
                    .residentKey(ResidentKeyRequirement.REQUIRED)
                    .build()
                )
                .timeout(timeout.toMillis())
                .build()
            );

        String optionsString = options.toCredentialsCreateJson();

        AuthenticatorRegistration registration = AuthenticatorRegistration.builder()
            .owner(identity)
            .options(options.toJson())
            .timeout(timeout.getSeconds())
            .build();

        em.persist(registration);
        em.flush();

        return new RegisterResult(registration.getId(), optionsString);
    }

    @Transactional
    public void completeRegistration(UUID owner, UUID registrationId, String credentialJson) throws JsonProcessingException {
        Identity identity = authService.getUser(owner);

        PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> response;
        try {
            response = PublicKeyCredential.parseRegistrationResponseJson(credentialJson);
        } catch (IOException e) {
            throw new InvalidCredential(e.getMessage(), e);
        }

        AuthenticatorRegistration registration = em
            .createNamedQuery("AuthenticatorRegistration.findByIdAndOwner", AuthenticatorRegistration.class)
            .setParameter("id", registrationId)
            .setParameter("owner", owner)
            .getResultStream()
            .findAny()
            .orElseThrow(() -> new InvalidCredential("No active registration"));

        PublicKeyCredentialCreationOptions request = PublicKeyCredentialCreationOptions.fromJson(registration.getOptions());

        RegistrationResult result;
        try {
            result = relyingPartyService
                .getRelyingParty()
                .finishRegistration(
                    FinishRegistrationOptions.builder()
                        .request(request)
                        .response(response)
                        .build()
                );
        } catch (RegistrationFailedException e) {
            throw new InvalidCredential(e.getMessage(), e);
        }

        em.remove(registration);
        Authenticator authenticator = Authenticator.builder()
            .owner(identity)
            .keyId(result.getKeyId().getId().getBytes())
            .cose(result.getPublicKeyCose().getBytes())
            .signatureCount(result.getSignatureCount())
            .build();

        em.persist(authenticator);
        em.flush();
    }

    @Transactional
    public Authenticator update(UUID owner, UUID authenticatorId, Consumer<Authenticator> updater) {
        Authenticator authenticator = getAuthenticator(owner, authenticatorId);

        updater.accept(authenticator);
        em.persist(authenticator);
        em.flush();

        return authenticator;
    }

    @Transactional
    public void remove(UUID owner, UUID authenticatorId) {
        em
            .createNamedQuery("Authenticator.findByIdAndOwner", Authenticator.class)
            .setParameter("id", authenticatorId)
            .setParameter("owner", owner)
            .getResultStream()
            .findAny()
            .ifPresent((authenticator) -> {
                em.remove(authenticator);
                em.flush();
            });
    }

    @Scheduled(every = "1h")
    @Transactional
    public void cleanUpOldAuthenticatorRegistrations() {
        em.createNativeQuery("""
                delete
                from auth_authenticator_registration as r
                where r.created + r.timeout * interval '1 second' < now()
                """)
            .executeUpdate();
    }
}
