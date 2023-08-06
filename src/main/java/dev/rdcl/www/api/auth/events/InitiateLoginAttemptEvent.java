package dev.rdcl.www.api.auth.events;

import java.net.URI;

public record InitiateLoginAttemptEvent(String email, URI callback, String sessionToken) {
}
