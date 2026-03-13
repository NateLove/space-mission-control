package com.demo.mission.resource;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {
        int status;
        if (e instanceof NotFoundException)        status = 404;
        else if (e instanceof BadRequestException) status = 400;
        else                                        status = 500;

        var body = Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status",    status,
            "error",     e.getMessage() != null ? e.getMessage() : "An unexpected error occurred"
        );

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
