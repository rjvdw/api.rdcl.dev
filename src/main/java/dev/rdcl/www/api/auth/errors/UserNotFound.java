package dev.rdcl.www.api.auth.errors;

public class UserNotFound extends RuntimeException {
    public UserNotFound() {
        super();
    }

    public UserNotFound(Throwable cause) {
        super(cause);
    }
}
