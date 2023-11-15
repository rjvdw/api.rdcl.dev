package dev.rdcl.www.api.auth.errors;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class InvalidCredentialMapper implements ExceptionMapper<InvalidCredential> {
    @Override
    public Response toResponse(InvalidCredential exception) {
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(exception.getMessage())
                .build();
    }
}
