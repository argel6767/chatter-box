package com.chatter_box.email_service.exception;

import io.quarkus.security.UnauthorizedException;
import io.smallrye.faulttolerance.api.RateLimitException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.resteasy.reactive.RestResponse;

import java.time.Instant;

@Provider
class ExceptionHandler implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception e) {
        ErrorResDto dto = new ErrorResDto(e.getMessage(), UtilFunctions.getTimeStamp());
        return Response.serverError().entity(dto).build();
    }
}

@Provider
class RateLimitExceptionHandler implements ExceptionMapper<RateLimitException> {
    @Override
    public Response toResponse(RateLimitException rle) {
        ErrorResDto dto = new ErrorResDto(rle.getMessage(), UtilFunctions.getTimeStamp());
        return RestResponse.ResponseBuilder.create(RestResponse.Status.TOO_MANY_REQUESTS, dto).build().toResponse();
    }
}

@Provider
class UnAuthorizedExceptionHandler implements ExceptionMapper<UnauthorizedException> {

    @Override
    public Response toResponse(UnauthorizedException ae) {
        ErrorResDto dto = new ErrorResDto(ae.getMessage(), UtilFunctions.getTimeStamp());
        return RestResponse.ResponseBuilder.create(RestResponse.Status.UNAUTHORIZED, dto).build().toResponse();
    }
}

@Provider
class BadRequestExceptionHandler implements ExceptionMapper<BadRequestException> {
    @Override
    public Response toResponse(BadRequestException bre) {
        ErrorResDto dto = new ErrorResDto(bre.getMessage(), UtilFunctions.getTimeStamp());
        return RestResponse.ResponseBuilder.create(RestResponse.Status.BAD_REQUEST, dto).build().toResponse();
    }
}

class UtilFunctions {
    public static String getTimeStamp() {
        Long time = Instant.now().toEpochMilli();
        return String.valueOf(time);
    }
}

