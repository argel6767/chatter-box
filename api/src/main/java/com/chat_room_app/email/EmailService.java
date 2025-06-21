package com.chat_room_app.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * houses the business logic of formatting an email
 */
@Log
@Service
@Lazy
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${support.email}")
    private String emailUsername;

    /*
     * sending verification email
     * takes in to whom its being sent to & subject and body
     */
    public void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(emailUsername);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        mailSender.send(message);
    }

    public void sendVerificationEmail(String code, String email) throws MessagingException {
        log.info("sending verification email for user " + email);
        String subject = "Verification Email";
        String body = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Verify Your Account</title>
                </head>
                <body>
                        <h1 style="margin: 0; color: white; font-size: 24px; font-weight: 700; letter-spacing: 0.5px;">INeedHousing</h1>
                      </div>
                      <div class="content">
                        <h1 class="title">Verify Your Email Address</h1>
                        <p class="text">Thanks for signing up! Please enter the verification code below to complete your account setup.</p>
                        <div class="code-container">
                          <div class="verification-code">%s</div>
                        </div>
                        <p class="text">This code will expire in 10 minutes. If you didn't request this code, you can safely ignore this email.</p>
                        <p class="help-text">Having trouble? Contact our support team.</p>
                      </div>
                      <div class="footer">
                        <p class="footer-text">© 2025 INeedHousing. All rights reserved.</p>
                </body>
                </html>""", code);
        sendEmail(email, subject, body);
    }

    public void sendResetPasswordEmail(String code, String email) throws MessagingException {
        log.info("sending forgot password email for user " + email);
        String subject = "Reset Password";
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
                        <p class="text">Please enter the verification code below to reset your password.</p>
                        <div class="code-container">
                          <div class="verification-code">%s</div>
                        </div>
                        <p class="text">This code will expire in 10 minutes. If you didn't request this code, you can safely ignore this email.</p>
                        <p class="help-text">Having trouble? Contact our support team.</p>
                      </div>
                      <div class="footer">
                        <p class="footer-text">© 2025 INeedHousing. All rights reserved.</p>
                </body>
                </html>""", code);
        sendEmail(email, subject, body);
    }
}
