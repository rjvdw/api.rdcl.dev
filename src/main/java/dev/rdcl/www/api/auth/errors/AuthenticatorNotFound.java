package dev.rdcl.www.api.auth.errors;

public class AuthenticatorNotFound extends RuntimeException {
    public AuthenticatorNotFound() {
        super();
    }

    public AuthenticatorNotFound(Throwable cause) {
        super(cause);
    }
}
