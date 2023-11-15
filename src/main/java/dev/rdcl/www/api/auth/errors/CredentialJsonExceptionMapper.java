package dev.rdcl.www.api.auth.errors;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CredentialJsonExceptionMapper implements ExceptionMapper<CredentialJsonException> {
    @Override
    public Response toResponse(CredentialJsonException exception) {
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(exception.getMessage())
                .build();
    }
}
