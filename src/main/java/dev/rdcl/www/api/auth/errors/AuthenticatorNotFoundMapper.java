package dev.rdcl.www.api.auth.errors;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AuthenticatorNotFoundMapper implements ExceptionMapper<AuthenticatorNotFound> {
    @Override
    public Response toResponse(AuthenticatorNotFound exception) {
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
