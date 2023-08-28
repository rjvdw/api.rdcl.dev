package dev.rdcl.www.api.auth.errors;

import com.fasterxml.jackson.core.JsonProcessingException;

public class CredentialJsonException extends RuntimeException {

    public CredentialJsonException(JsonProcessingException e) {
        super(e);
    }
}
