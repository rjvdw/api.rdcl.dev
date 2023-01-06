package dev.rdcl.www.api.auth.errors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class LoginAttemptNotFoundMapper implements ExceptionMapper<LoginAttemptNotFound> {
    @Override
    public Response toResponse(LoginAttemptNotFound exception) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
