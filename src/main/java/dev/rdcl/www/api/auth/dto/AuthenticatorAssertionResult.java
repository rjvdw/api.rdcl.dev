package dev.rdcl.www.api.auth.dto;

import java.util.UUID;

public record AuthenticatorAssertionResult(UUID id, String options) {
}
