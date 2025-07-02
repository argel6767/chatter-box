package com.chatter_box.email_service.email;

import com.chatter_box.email_service.email.dto.VerificationCodeDto;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/emails")
public class EmailResource {

    @Inject
    EmailService emailService;

    @POST
    @Path("/verify")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Void> sendVerificationEmail(VerificationCodeDto request) {
        return emailService.sendVerificationEmail(request.email(), request.username(), request.code());
    }

    @POST
    @Path("/reset-password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Void> sendResetPasswordEmail(VerificationCodeDto request) {
        return emailService.sendResetPasswordEmail(request.email(), request.username(), request.code());
    }

}
