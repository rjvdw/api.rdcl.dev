package dev.rdcl.www.api.auth;


import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import dev.rdcl.www.api.auth.entities.Authenticator;
import dev.rdcl.www.api.auth.entities.Identity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
@RequiredArgsConstructor
public class AuthenticatorRepository implements CredentialRepository {

    private final EntityManager em;

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String email) {
        return em
            .createNamedQuery("Identity.findByEmail", Identity.class)
            .setParameter("email", email)
            .getResultStream()
            .map(Identity::getAuthenticators)
            .flatMap(List::stream)
            .map(a -> PublicKeyCredentialDescriptor.builder()
                .id(new ByteArray(a.getKeyId()))
                .build())
            .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String email) {
        return em
            .createNamedQuery("Identity.findByEmail", Identity.class)
            .setParameter("email", email)
            .getResultStream()
            .map(Identity::getId)
            .map(Mappers::uuidToByteArray)
            .findAny();
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        return Optional.of(userHandle)
            .map(Mappers::byteArrayToUuid)
            .flatMap(id -> em
                .createNamedQuery("Identity.findById", Identity.class)
                .setParameter("id", id)
                .getResultStream()
                .findAny()
            )
            .map(Identity::getEmail);
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return Stream.of(userHandle)
            .map(Mappers::byteArrayToUuid)
            .flatMap(id -> em
                .createNamedQuery("Identity.findById", Identity.class)
                .setParameter("id", id)
                .getResultStream()
            )
            .flatMap(i -> i.getAuthenticators()
                .stream()
                .map(a -> new Tuple(i.getId(), a))
            )
            .filter(tuple -> Arrays.equals(tuple.authenticator().getKeyId(), credentialId.getBytes()))
            .map(Tuple::map)
            .findAny();
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return em
            .createNamedQuery("Authenticator.findByKeyId", Authenticator.class)
            .setParameter("keyId", credentialId.getBytes())
            .getResultStream()
            .map(a -> new Tuple(a.getOwner().getId(), a))
            .map(Tuple::map)
            .collect(Collectors.toSet());
    }

    private record Tuple(UUID id, Authenticator authenticator) {
        private RegisteredCredential map() {
            return RegisteredCredential.builder()
                .credentialId(new ByteArray(authenticator.getKeyId()))
                .userHandle(Mappers.uuidToByteArray(id))
                .publicKeyCose(new ByteArray(authenticator.getCose()))
                .build();
        }
    }
}
