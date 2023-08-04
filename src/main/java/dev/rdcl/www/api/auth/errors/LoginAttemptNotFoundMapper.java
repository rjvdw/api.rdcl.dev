package dev.rdcl.www.api.auth.errors;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class LoginAttemptNotFoundMapper implements ExceptionMapper<LoginAttemptNotFound> {
    @Override
    public Response toResponse(LoginAttemptNotFound exception) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
