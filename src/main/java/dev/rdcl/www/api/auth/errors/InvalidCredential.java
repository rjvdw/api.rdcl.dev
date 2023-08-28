package dev.rdcl.www.api.auth.errors;

public class InvalidCredential extends RuntimeException {

    public InvalidCredential() {
        super("Invalid credential provided");
    }

    public InvalidCredential(String reason) {
        super("Invalid credential provided: " + reason);
    }

    public InvalidCredential(Throwable cause) {
        super("Invalid credential provided", cause);
    }

    public InvalidCredential(String reason, Throwable cause) {
        super("Invalid credential provided: " + reason, cause);
    }
}
