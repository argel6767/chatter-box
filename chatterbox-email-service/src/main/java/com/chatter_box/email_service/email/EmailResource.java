package com.chatter_box.email_service.email;

import com.chatter_box.email_service.email.dto.VerificationCodeDto;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.faulttolerance.api.RateLimit;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.logging.Log;


import java.time.temporal.ChronoUnit;

@Path("/api/v1/emails")
public class EmailResource {

    @Inject
    EmailService emailService;

    @Inject
    @ConfigProperty(name = "secret.key")
    String secretKey;

    @POST
    @RateLimit(value = 20, window = 5, windowUnit = ChronoUnit.MINUTES)
    @Path("/verify")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Void> sendVerificationEmail(@HeaderParam("Access-Token") String accessToken, VerificationCodeDto request) {
        verifyAccessToken(accessToken);
        verifyRequestBody(request);
        return emailService.sendVerificationEmail(request.email(), request.username(), request.code());
    }

    @POST
    @RateLimit(value = 20, window = 5, windowUnit = ChronoUnit.MINUTES)
    @Path("/reset-password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Void> sendResetPasswordEmail(@HeaderParam("Access-Token") String accessToken, VerificationCodeDto request) {
        verifyAccessToken(accessToken);
        verifyRequestBody(request);
        return emailService.sendResetPasswordEmail(request.email(), request.username(), request.code());
    }

    private void verifyAccessToken(String accessToken) {
        if (accessToken == null || !accessToken.equals(secretKey)) {
            throw new UnauthorizedException("Invalid access token");
        }
    }

    private void verifyRequestBody(VerificationCodeDto request) {
        if (request.email() == null || request.username() == null || request.code() == null) {
            throw new BadRequestException("Invalid request");
        }
    }

}
