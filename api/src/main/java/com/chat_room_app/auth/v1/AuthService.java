package com.chat_room_app.auth.v1;

import com.chat_room_app.auth.AuthDetails;
import com.chat_room_app.auth.AuthRepository;
import com.chat_room_app.auth.dtos.*;
import com.chat_room_app.email.EmailService;
import com.chat_room_app.exceptions.custom_exceptions.BadRequest400Exception;
import com.chat_room_app.exceptions.custom_exceptions.Conflict409Exception;
import com.chat_room_app.exceptions.custom_exceptions.NotFound404Exception;
import com.chat_room_app.exceptions.custom_exceptions.UnAuthorized401Exception;
import com.chat_room_app.users.User;
import com.chat_room_app.users.UserRepository;
import jakarta.mail.MessagingException;
import lombok.extern.java.Log;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.chat_room_app.auth.AuthUtil.*;

/**
 * Holds the business logic for authenticating users and sending emails for verification codes
 */
@Log
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthRepository authRepository;

    public AuthService(UserRepository userRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, EmailService emailService, AuthRepository authRepository) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.authRepository = authRepository;
    }

    /**
     * signs up user to app, will fail if the email or username is taken as they need to be unique
     */
    public User signUp(RegisterUserDto request) throws MessagingException {
        log.info("Signing up user");
        verifyRegistrationDetails(request, userRepository);
        log.info("Creating new user " + request.username());
        User user = new User(request.username().toLowerCase(), request.email().toLowerCase(), passwordEncoder.encode(request.password()));
        AuthDetails authDetails = user.getAuthDetails();
        authDetails.setAuthorities("ROLE_USER");
        String code = setVerificationCode(authDetails);
        sendVerificationEmail(user, code);
        log.info("User " + request.username() + " created");
        return userRepository.save(user);
    }

    /**
     * authenticates user, usually when they are logging in
     * will throw an exception if the email is not tied to any user or the email has not been verified
     */
    public User authenticateUser(AuthenticateUserDto request) {
        log.info("Authenticating user " + request.username());
        User user = getUserByUsername(request.username().toLowerCase());
        AuthDetails authDetails = user.getAuthDetails();
        if (!authDetails.getIsVerified()) {
            log.warning("user " + request.username() + " is not verified");
            throw new UnAuthorized401Exception("user is not verified: " + request.username());
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        user.setLastLoggedIn(LocalDateTime.now());
        log.info("Authenticated user " + request.username());
        return userRepository.save(user);
    }

    /**
     * verifies user by checking if request token is equal to the one in db
     * if so then the codeExpiry is set to null
     * and isEmailVerified to true
     */
    public void verifyUser(VerifyUserDto request) {
        User user = getUser(request.email());
        AuthDetails authDetails = user.getAuthDetails();
        if (authDetails.getIsVerified()) {
            log.warning("user already verified: " + request.username());
            throw new Conflict409Exception("User is already verified");
        }
        if (authDetails.getCodeExpiryTime().isBefore(LocalDateTime.now())) {
            log.warning("verification code expired: " + request.code());
            throw new BadRequest400Exception("Verification code expired");
        }
        log.info("verifying user submitting token");
        if (request.code().equals(authDetails.getVerificationCode())) {
            authDetails.setCodeExpiryTime(null);
            authDetails.setIsVerified(true);
            log.info("user verified");
            userRepository.save(user);
        }
        else {
            log.warning("verification code invalid: " + request.code());
            throw new BadRequest400Exception("Verification code is invalid");
        }

    }

    /**
     * Grabs user from repository
     * @param email
     * @return
     */
    private User getUser(String email) {
        log.info("getting user " + email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFound404Exception("No user found for email " + email));
    }

    private User getUserByUsername(String username) {
        log.info("getting user " + username);
        return  userRepository.findByUsername(username).orElseThrow(() -> new NotFound404Exception("No user found for username: " + username));
    }


    /**
     * resends verification email to user but with new code
     * can be used if their last code expired
     */
    public void resendVerificationEmail(String username) throws MessagingException {
        User user = getUserByUsername(username);
        AuthDetails authDetails = user.getAuthDetails();
        if (authDetails.getIsVerified()) {
            log.warning("user already verified: " + username);
            throw new Conflict409Exception("Email is already verified");
        }
        log.info("resending verification email with new code" + username);
        String code = setVerificationCode(authDetails);
        sendVerificationEmail(user, code);
        userRepository.save(user);
    }

    /**
     * changes user's password to new one, only if:
     * they exist and if they send their correct current password
     */
    public User changePassword(ChangePasswordDto request) {
        log.info("Changing password for" + request.username());
        User user = getUserByUsername(request.username());
        String oldPassword = user.getPassword();
        if (!passwordEncoder.matches(request.oldPassword(), oldPassword)) {
            log.warning("old password does not match password hash");
            throw new BadRequest400Exception("Current password is not correct");
        }
        if(!isValidPassword(request.newPassword())){
            log.warning("new password is not valid");
            throw new BadRequest400Exception("New password is not valid");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        log.info("saving new password");
        return userRepository.save(user);
    }

    /**
     * sends a user a verification code for changing one's password, should they forget it.
     */

    public void sendForgottenPasswordVerificationCode(String username) throws MessagingException {
        log.info("sending forgotten password verification code for user " + username);
        User user = getUserByUsername(username);
        AuthDetails authDetails = user.getAuthDetails();
        String code = setVerificationCode(authDetails);
        sendResetPasswordEmail(user, code);
        userRepository.save(user);
    }

    /**
     * resets a user's password, only if:
     * code is not expired
     * and verification code is correct one in db
     */
    public User resetPassword(ForgetPasswordDto request) {
        log.info("resetting password for " + request.username());
        User user = getUserByUsername(request.username());
        AuthDetails authDetails = user.getAuthDetails();
        if (authDetails.getCodeExpiryTime().isBefore(LocalDateTime.now())) {
            log.warning("verification code expired: " + request.username());
            throw new BadRequest400Exception("Verification code expired, request another one");
        }
        if (!authDetails.getVerificationCode().equals(request.verificationCode())) {
            log.warning("verification code invalid: " + request.verificationCode());
            throw new BadRequest400Exception("Invalid verification code");
        }
        if (!isValidPassword(request.newPassword())) {
            log.warning("new password is not valid");
            throw new BadRequest400Exception("new password is not valid");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        authDetails.setCodeExpiryTime(null);
        authDetails.setVerificationCode(null);
        log.info("saving new password");
        return userRepository.save(user);
    }

    /**
     * formats and generates the verification email that will contain the verification code to the user
     */
    private void sendVerificationEmail(User user, String code) throws MessagingException {
        String email = user.getEmail();
        emailService.sendVerificationEmail(code, email);
        log.info("verification email sent for user " + user.getEmail());
    }

    /**
     * formats and generates the reset password email that will contain the verification code to the user
     */
    private void sendResetPasswordEmail(User user, String code) throws MessagingException {
        String email = user.getEmail();
        emailService.sendResetPasswordEmail(code,email);
        log.info("reset forgot password email sent for user " + user.getEmail());
    }

}
