package dev.rdcl.www.api.auth.errors;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UserNotFoundMapper implements ExceptionMapper<UserNotFound> {
    @Override
    public Response toResponse(UserNotFound exception) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
