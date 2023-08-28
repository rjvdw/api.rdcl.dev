package dev.rdcl.www.api.auth.dto;

import java.util.UUID;

public record InitiateLoginResult(LoginMode mode, String payload, UUID assertionId) {
}
