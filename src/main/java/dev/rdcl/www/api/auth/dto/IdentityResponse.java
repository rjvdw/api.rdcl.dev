package dev.rdcl.www.api.auth.dto;

import dev.rdcl.www.api.auth.entities.Identity;

public record IdentityResponse(String name, String email) {
    public static IdentityResponse from(Identity identity) {
        return new IdentityResponse(
            identity.getName(),
            identity.getEmail()
        );
    }
}
