package com.chat_room_app.auth.v2;

import com.chat_room_app.auth.AuthDetails;
import com.chat_room_app.auth.dtos.RegisterUserDto;
import com.chat_room_app.auth.dtos.VerifyUserDto;
import com.chat_room_app.email.ChatterBoxEmailService;
import com.chat_room_app.exceptions.custom_exceptions.Conflict409Exception;
import com.chat_room_app.exceptions.custom_exceptions.NotFound404Exception;
import com.chat_room_app.exceptions.custom_exceptions.ServiceUnavailableException;
import com.chat_room_app.users.User;
import com.chat_room_app.users.UserRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.chat_room_app.auth.AuthUtil.*;

@Service
@Log
public class AuthV2Service {
    private final Executor virtualThreadExecutor;
    private final ChatterBoxEmailService chatterBoxEmailService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthV2Service(@Qualifier("virtualThreadExecutor") Executor virtualThreadExecutor, ChatterBoxEmailService chatterBoxEmailService, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.virtualThreadExecutor = virtualThreadExecutor;
        this.chatterBoxEmailService = chatterBoxEmailService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    /**
     * Registers users and calls email service asynchronous to avoid blocking
     * @param request
     */
    public void signUp(RegisterUserDto request) {
        log.info("Registering new user");
        verifyRegistrationDetails(request, userRepository);
        User user = new User(request.username().toLowerCase(), request.email().toLowerCase(), passwordEncoder.encode(request.password()));
        AuthDetails authDetails = user.getAuthDetails();
        authDetails.setAuthorities("ROLE_USER");
        String code = setVerificationCode(authDetails);
        CompletableFuture.runAsync(() -> {
            log.info("Sending email verification request to external email service");
            try {
                VerifyUserDto emailServiceDto = new VerifyUserDto(request.email(), request.username(), code);
                chatterBoxEmailService.sendVerifyEmail(emailServiceDto);
                log.info("Email request sent successfully");
            }
            catch (Exception e) {
                log.warning("Failed to POST to email service, and send verification email");
            }
        }, virtualThreadExecutor);
        userRepository.save(user);
    }

    public void resendVerificationEmail(String username) {
        User user = getUserByUsername(username);
        if (user.isEnabled()) {
            log.warning("user already verified: " + username);
            throw new Conflict409Exception("Email is already verified");
        }
        AuthDetails authDetails = user.getAuthDetails();
        String code = setVerificationCode(authDetails);
        CompletableFuture.runAsync(() -> {
            log.info("Sending email verification request to external email service");
            try {
                VerifyUserDto emailServiceDto = new VerifyUserDto(user.getEmail(), username, code);
                chatterBoxEmailService.sendVerifyEmail(emailServiceDto);
            }
            catch (Exception e) {
                log.warning("Failed to POST to email service, and resend verification email");
            }
        }, virtualThreadExecutor);
        //userRepository.save(user);
    }


    /**
     * Sends a forget password request to email service asynchronous to avoid blocking
     * @param username
     */
    public void sendForgotPasswordVerificationCode(String username) {
        log.info("sending forgotten password verification code for user " + username);
        User user = getUserByUsername(username);
        AuthDetails authDetails = user.getAuthDetails();
        String code = setVerificationCode(authDetails);
        CompletableFuture.runAsync(() -> {
            log.info("Sending forgot password request to external email service");
            try {
                VerifyUserDto emailServiceDto = new VerifyUserDto(user.getEmail(), username, code);
                chatterBoxEmailService.sendRestPasswordEmail(emailServiceDto);
            }
            catch (Exception e) {
                log.warning("Failed to POST to email service, and send forget password verification email");
            }
        }, virtualThreadExecutor);
        userRepository.save(user);
    }

    private User getUserByUsername(String username) {
        String normalizedUsername = username.toLowerCase();
        log.info("getting user " + username);
        return  userRepository.findByUsername(username).orElseThrow(() -> new NotFound404Exception("No user found for username: " + normalizedUsername));
    }

}
