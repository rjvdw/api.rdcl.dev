package dev.rdcl.www.api.auth.errors;

public class InvalidCallback extends RuntimeException {

    public InvalidCallback(String callback) {
        super("Invalid callback provided: %s".formatted(callback));
    }

    public InvalidCallback(String callback, Throwable cause) {
        super("Invalid callback provided: %s".formatted(callback), cause);
    }
}
