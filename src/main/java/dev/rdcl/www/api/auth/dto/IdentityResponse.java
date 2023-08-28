package dev.rdcl.www.api.auth.dto;

import dev.rdcl.www.api.auth.entities.Identity;

import java.util.List;

public record IdentityResponse(String name, String email, List<AuthenticatorResponse> authenticators) {
    public static IdentityResponse from(Identity identity) {
        return new IdentityResponse(
            identity.getName(),
            identity.getEmail(),
            identity.getAuthenticators().stream().map(AuthenticatorResponse::from).toList()
        );
    }
}
