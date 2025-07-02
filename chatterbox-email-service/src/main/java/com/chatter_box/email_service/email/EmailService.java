package com.chatter_box.email_service.email;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class EmailService {

    @Inject
    ReactiveMailer reactiveMailer;

    @Inject
    @ConfigProperty(name = "frontend.domain")
    String domain;

    public Uni<Void> sendEmail(String to, String subject, String body) {
        Mail mail = Mail.withHtml(to, subject, body);
        return reactiveMailer.send(mail);
    }

    public Uni<Void> sendVerificationEmail(String userEmail, String username, String code) {
        String subject = "Verification Email";
        String verificationLink = String.format(domain+"verify?email=%s&username=%s&code=%s", userEmail, username, code);
        String body = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Verify Your Account</title>
                </head>
                <body>
                        <h1 style="margin: 0; color: white; font-size: 24px; font-weight: 700; letter-spacing: 0.5px;">ChatterBox</h1>
                      </div>
                      <div class="content">
                        <h1 class="title">Verify Your Email Address</h1>
                        <p class="text">Thanks for signing up %s! Please click the link below to complete your account setup.</p>
                        <div class="code-container">
                          <a class="verification-code" href="%s">Verify</div>
                        </div>
                        <p class="text">This code will expire in 10 minutes. If you didn't request this code, you can ignore this email, or reply to have your email deleted</p>
                        <p class="help-text">Having trouble? Contact our support team.</p>
                      </div>
                      <div class="footer">
                        <p class="footer-text">© 2025 ChatterBox. All rights reserved.</p>
                </body>
                </html>""", username, verificationLink);
        return sendEmail(userEmail, subject, body);
    }

    public Uni<Void> sendResetPasswordEmail(String userEmail, String username, String code) {
        String subject = "Reset Password";
        String verificationLink = String.format(domain+"/verify?email=%s&username=%s&code=%s", userEmail, username, code);
        String body = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Forgot Your Password?</title>
                </head>
                <body>
                        <h1 style="margin: 0; color: white; font-size: 24px; font-weight: 700; letter-spacing: 0.5px;">INeedHousing</h1>
                      </div>
                      <div class="content">
                        <h1 class="title">Forgot Your Password</h1>
                        <p class="text">Hello %s! Please enter the verification code below to reset your password.</p>
                        <div class="code-container">
                          <a class="verification-code" href="%s">Verify</div>
                        </div>
                        <p class="text">This code will expire in 10 minutes. If you didn't request this code, you can safely ignore this email.</p>
                        <p class="help-text">Having trouble? Contact our support team.</p>
                      </div>
                      <div class="footer">
                        <p class="footer-text">© 2025 ChatterBox. All rights reserved.</p>
                </body>
                </html>""", username, verificationLink);
        return  sendEmail(userEmail, subject, body);
    }
}
