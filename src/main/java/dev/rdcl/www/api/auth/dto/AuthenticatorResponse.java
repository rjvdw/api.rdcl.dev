package dev.rdcl.www.api.auth.dto;

import dev.rdcl.www.api.auth.entities.Authenticator;

import java.util.UUID;

public record AuthenticatorResponse(UUID id, String name) {
    public static AuthenticatorResponse from(Authenticator authenticator) {
        return new AuthenticatorResponse(
            authenticator.getId(),
            authenticator.getName()
        );
    }
}
