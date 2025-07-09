package com.chat_room_app.auth.v1;


import com.chat_room_app.auth.dtos.*;
import com.chat_room_app.jwt.JwtService;
import com.chat_room_app.jwt.JwtUtils;
import com.chat_room_app.users.User;
import com.chat_room_app.users.dtos.UserDto;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * holds auth endpoints that can be accessed without a JWT token
 */
@Log
@RequestMapping("/api/v1/auths")
@RestController
public class AuthController {

    private final AuthService authenticationService;
    private final JwtService jwtService;

    public AuthController(AuthService authenticationService, JwtService jwtService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }

    /**
     * used to check if cookie is still valid, will return 200 if is
     * it won't even be run if it is not due the security chain and return a 403
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cookie-status")
    @RateLimiter(name = "auths")
    public ResponseEntity<String> checkCookie() {
        String username = JwtUtils.getCurrentUserUsername();
        log.info("Cookie is still valid for user: " + username);
        return new ResponseEntity<>("Token still valid", HttpStatus.OK);
    }

    /**
     * register user endpoint
     */
    @PostMapping("/register")
    @RateLimiter(name = "auths")
    public ResponseEntity<UserDto> register(@RequestBody RegisterUserDto request) throws MessagingException {
            User registeredUser = authenticationService.signUp(request);
            UserDto dto = UserDto.getUserDto(registeredUser);
            return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    /**
     * login user endpoint
     */
    @PostMapping("/login")
    @RateLimiter(name = "auths")
    public ResponseEntity<UserDto> login(@RequestBody AuthenticateUserDto request, HttpServletResponse response) {
        User user = authenticationService.authenticateUser(request);
        String token = jwtService.generateToken(user);
        String cookieHeader = jwtService.generateCookie(token, Optional.empty());
        response.setHeader("Set-Cookie", cookieHeader);
        UserDto userDto = UserDto.getUserDto(user);
        return ResponseEntity.ok(userDto);
    }

    /**
     * verify user endpoint via the code they input
     */
    @PostMapping("/verify")
    @RateLimiter(name = "auths")
    public ResponseEntity<String> verify(@RequestBody VerifyUserDto request) {
        authenticationService.verifyUser(request);
        return ResponseEntity.ok("User verified!");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    @RateLimiter(name = "auths")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // Set cookie header with SameSite
        String cookieHeader = jwtService.generateCookie("", Optional.of(0L));
        response.setHeader("Set-Cookie", cookieHeader);
        return ResponseEntity.ok("Logged out successfully");
    }

    /**
     * resend verification email endpoint
     */
    @PostMapping("/resend/{username}")
    @RateLimiter(name = "auths")
    public ResponseEntity<EmailSentSuccessfullyDto> resend(@PathVariable String username) throws MessagingException {
        authenticationService.resendVerificationEmail(username);
        EmailSentSuccessfullyDto dto = new EmailSentSuccessfullyDto(username, "Verification code resent!");
        return ResponseEntity.ok(dto);
    }

    /**
     * changes passwords for user
     * THIS IS USED ONLY FOR WHEN A USER WANTS TO UPDATE PASSWORD
     */
    @PutMapping("/password")
    @RateLimiter(name = "auths")
    public ResponseEntity<UserDto> changePassword(@RequestBody ChangePasswordDto request) {
        User user = authenticationService.changePassword(request);
        UserDto userDto = UserDto.getUserDto(user);
        return ResponseEntity.ok(userDto);
    }

    /**
     * sends email for reset password request
     */
    @PostMapping("/forgot/{username}")
    @RateLimiter(name = "auths")
    public ResponseEntity<EmailSentSuccessfullyDto> forgotPassword(@PathVariable String username) throws MessagingException {
        authenticationService.sendForgottenPasswordVerificationCode(username);
        EmailSentSuccessfullyDto dto = new EmailSentSuccessfullyDto(username, "Forgot password verification code sent!");
        return ResponseEntity.ok(dto);
    }

    /**
     * resets password
     * ONLY TO BE USED FOR WHEN USER FORGETS PASSWORD
     */
    @PutMapping("/reset")
    @RateLimiter(name = "auths")
    public ResponseEntity<UserDto> resetPasswordForgottenPassword(@RequestBody ForgetPasswordDto request) {
        User user = authenticationService.resetPassword(request);
        UserDto userDto = UserDto.getUserDto(user);
        return ResponseEntity.ok(userDto);
    }

}
