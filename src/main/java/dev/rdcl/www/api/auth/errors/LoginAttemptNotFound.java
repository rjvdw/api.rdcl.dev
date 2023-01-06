package dev.rdcl.www.api.auth.errors;

public class LoginAttemptNotFound extends RuntimeException {
    public LoginAttemptNotFound() {
        super();
    }

    public LoginAttemptNotFound(Throwable cause) {
        super(cause);
    }
}
