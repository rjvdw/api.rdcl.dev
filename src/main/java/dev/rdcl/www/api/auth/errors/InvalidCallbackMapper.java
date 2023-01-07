package dev.rdcl.www.api.auth.errors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InvalidCallbackMapper implements ExceptionMapper<InvalidCallback> {
    @Override
    public Response toResponse(InvalidCallback exception) {
        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(exception.getMessage())
            .build();
    }
}
