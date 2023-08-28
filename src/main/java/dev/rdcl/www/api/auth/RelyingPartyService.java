package dev.rdcl.www.api.auth;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class RelyingPartyService {

    private final RelyingPartyProperties relyingPartyProperties;

    private final AuthenticatorRepository authenticatorRepository;

    public RelyingParty getRelyingParty() {
        RelyingPartyIdentity identity = RelyingPartyIdentity.builder()
            .id(relyingPartyProperties.id())
            .name(relyingPartyProperties.name())
            .build();

        return RelyingParty.builder()
            .identity(identity)
            .credentialRepository(authenticatorRepository)
            .origins(relyingPartyProperties.origins())
            .allowOriginSubdomain(relyingPartyProperties.allowOriginSubdomain())
            .build();
    }

}
