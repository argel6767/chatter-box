package com.chat_room_app.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.chat_room_app.auth.dtos.AuthenticateUserDto;
import com.chat_room_app.auth.dtos.ChangePasswordDto;
import com.chat_room_app.auth.dtos.ForgetPasswordDto;
import com.chat_room_app.auth.dtos.RegisterUserDto;
import com.chat_room_app.auth.dtos.VerifyUserDto;
import com.chat_room_app.email.EmailService;
import com.chat_room_app.jwt.JwtService;
import com.chat_room_app.users.User;
import com.chat_room_app.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import lombok.extern.java.Log;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@Log
class AuthControllerTest {

    @Autowired private MockMvc mvc;

    @Autowired private ObjectMapper mapper;

    @Autowired private UserRepository userRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    @MockBean private EmailService emailService;

    @MockBean private JwtService jwtService;

    @MockBean private AuthenticationManager authenticationManager;

    /* --------------------------------------------------------------------- */
    /* ------------------------------ helpers ------------------------------ */
    /* --------------------------------------------------------------------- */

    private String toJson(Object o) throws Exception {
        return mapper.writeValueAsString(o);
    }

    /* --------------------------------------------------------------------- */
    /* ------------------------------- tests ------------------------------- */
    /* --------------------------------------------------------------------- */

    @Test
    @DisplayName("POST /api/v1/auths/cookie-status → 200 OK")
    void checkCookieStatus_verifiesUsersJWT() throws Exception {
        User user = new User("john", "john@email.com", passwordEncoder.encode("Password1!"));
        userRepository.save(user);
        String token = jwtService.generateToken(user);
        log.info("token: " + token);
        String cookie = jwtService.generateCookie(token, Optional.empty());
        log.info("cookie: " + cookie);
        mvc.perform(
                        post("/api/v1/auths/cookie-status")
                                .cookie(new Cookie("access-token", cookie)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/auths/logout → 200 OK")
    void logout_logsOutUser() throws Exception {
        User user = new User("john", "john@email.com", passwordEncoder.encode("Password1!"));
        userRepository.save(user);
        String token = jwtService.generateToken(user);
        log.info("token: " + token);
        String cookie = jwtService.generateCookie(token, Optional.of(0L));
        log.info("cookie: " + cookie);
        mvc.perform(
                        post("/api/v1/auths/cookie-logout")
                                .cookie(new Cookie("Set-Cookie", cookie)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/auths/register → 201 CREATED and email sent")
    void register_createsUser() throws Exception {
        RegisterUserDto body = new RegisterUserDto("john@mail.com", "john", "Password1!");

        mvc.perform(
                        post("/api/v1/auths/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.email").value("john@mail.com"));

        assertThat(userRepository.findByUsername("john")).isPresent();
        verify(emailService, times(1)).sendVerificationEmail(anyString(), eq("john@mail.com"));
    }

    @Test
    @DisplayName("Registering with an invalid email → 400 BAD REQUEST")
    void register_failsWithInvalidEmail() throws Exception {
        RegisterUserDto body = new RegisterUserDto("invalidEmail", "john", "Password1!");

        mvc.perform(
                        post("/api/v1/auths/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(body.email() + " is an invalid email"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auths/register"));

        assertThat(userRepository.findByUsername("john")).isEmpty();
        verify(emailService, times(0)).sendVerificationEmail(anyString(), eq("john@mail.com"));
    }

    @Test
    @DisplayName("Registering with an invalid password → 400 BAD REQUEST")
    void register_failsWithInvalidPassword() throws Exception {
        RegisterUserDto body = new RegisterUserDto("email@example.com", "john", "invalidPassword");

        mvc.perform(
                        post("/api/v1/auths/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(body.password() + " is an invalid password"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auths/register"));

        assertThat(userRepository.findByUsername("john")).isEmpty();
        verify(emailService, times(0)).sendVerificationEmail(anyString(), eq("john@mail.com"));
    }

    @Test
    @DisplayName("Registering twice with same username → 409 CONFLICT")
    void register_duplicateUsername() throws Exception {
        // first registration
        userRepository.save(new User("john", "john@mail.com", passwordEncoder.encode("Password1!")));

        RegisterUserDto body = new RegisterUserDto("another@mail.com", "john", "Password1!");

        mvc.perform(
                        post("/api/v1/auths/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorMessage").value(body.username() + " is already in use"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auths/register"));
    }

    @Test
    @DisplayName("Registering twice with same email → 409 CONFLICT")
    void register_duplicateEmail() throws Exception {
        // first registration
        userRepository.save(new User("john", "john@mail.com", passwordEncoder.encode("Password1!")));

        RegisterUserDto body = new RegisterUserDto("john@mail.com", "john123", "Password1!");

        mvc.perform(
                        post("/api/v1/auths/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorMessage").value(body.email() + " is already in use"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auths/register"));
    }

    @Test
    @DisplayName("POST /api/v1/auths/login → 200 OK, Set-Cookie + UserDto")
    void login_returnsCookieAndUserDto() throws Exception {
        /* ---------- GIVEN a verified user in the DB ---------- */
        User user = new User("john", "john@mail.com", passwordEncoder.encode("Password1!"));
        AuthDetails auth = new AuthDetails();
        auth.setIsVerified(true);
        auth.setAuthorities("ROLE_USER");
        user.setAuthDetails(auth);
        userRepository.save(user);

        // Token + cookie generated by mocked service
        when(jwtService.generateToken(any(User.class))).thenReturn("dummy-jwt-token");
        when(jwtService.generateCookie(eq("dummy-jwt-token"), any()))
                .thenReturn("accessToken=dummy-jwt-token; Path=/; HttpOnly");

        AuthenticateUserDto body = new AuthenticateUserDto("john", "Password1!");

        mvc.perform(
                        post("/api/v1/auths/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(body)))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("accessToken=dummy-jwt-token")))
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.email").value("john@mail.com"));
    }

    @Test
    @DisplayName("Login with non-existent user → 404 NOT FOUND")
    void login_returnsNotFound() throws Exception {
        AuthenticateUserDto body = new AuthenticateUserDto("john", "Password1!");

        mvc.perform(
                        post("/api/v1/auths/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value("No user found for username: " + body.username()))
                .andExpect(jsonPath("$.instance").value("/api/v1/auths/login"));
    }

    @Test
    @DisplayName("Login with non-verified user → 401 UNAUTHORIZED")
    void login_returnsUnauthorized() throws Exception {
        /* ---------- GIVEN a verified user in the DB ---------- */
        User user = new User("john", "john@mail.com", passwordEncoder.encode("Password1!"));
        AuthDetails auth = new AuthDetails();
        
        user.setAuthDetails(auth);
        userRepository.save(user);

        AuthenticateUserDto body = new AuthenticateUserDto("john", "Password1!");

        mvc.perform(
                        post("/api/v1/auths/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorMessage").value("user is not verified: " + body.username()))
                .andExpect(jsonPath("$.instance").value("/api/v1/auths/login"));
    }

    @Test
    @DisplayName("Login with wrong credentials → 403 FORBIDDEN")
    void login_returnsBadCredentials() throws Exception {
        /* ---------- GIVEN a verified user in the DB ---------- */
        User user = new User("john", "john@mail.com", passwordEncoder.encode("Password1!"));
        AuthDetails auth = new AuthDetails();
        auth.setAuthorities("ROLE_USER");
        auth.setIsVerified(true);
        user.setAuthDetails(auth);
        userRepository.save(user);

        AuthenticateUserDto body = new AuthenticateUserDto("john", "wrongPassword");

        mvc.perform(
                        post("/api/v1/auths/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.instance").value("/api/v1/auths/login"));
    }

    @Test
    @DisplayName("POST /api/v1/auths/verify → 200 OK, user becomes verified")
    void verifyUser_success() throws Exception {
        /* ---------- GIVEN an un-verified user with code ---------- */
        User user = new User("john", "john@mail.com", passwordEncoder.encode("Password1!"));
        AuthDetails auth = new AuthDetails();
        
        auth.setVerificationCode("123456");
        auth.setCodeExpiryTime(LocalDateTime.now().plusMinutes(10));
        user.setAuthDetails(auth);
        auth.setAuthorities("ROLE_USER");
        userRepository.save(user);

        VerifyUserDto body = new VerifyUserDto("john@mail.com", "username","123456");

        mvc.perform(
                        post("/api/v1/auths/verify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(body)))
                .andExpect(status().isOk())
                .andExpect(content().string("User verified!"));

        assertThat(
                userRepository
                        .findByUsername("john")
                        .orElseThrow()
                        .getAuthDetails()
                        .getIsVerified())
                .isTrue();
    }

    @Test
    @DisplayName("Verify an already verified user → 409 CONFLICT")
    void verifyUser_alreadyVerified() throws Exception {
        User user = new User("john", "john@mail.com", passwordEncoder.encode("Password1!"));
        AuthDetails auth = new AuthDetails();
        
        auth.setIsVerified(true);
        user.setAuthDetails(auth);
        auth.setAuthorities("ROLE_USER");
        userRepository.save(user);

        VerifyUserDto body = new VerifyUserDto("john@mail.com", "username", "123456");

        mvc.perform(
                        post("/api/v1/auths/verify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.instance").value("/api/v1/auths/verify"))
                .andExpect(jsonPath("$.errorMessage").value("User is already verified"));
    }

    @Test
    @DisplayName("verify user with expired code → 400 BAD REQUEST")
    void verifyUser_expiredCode() throws Exception {
        User user = new User("john", "john@mail.com", passwordEncoder.encode("Password1!"));
        AuthDetails auth = new AuthDetails();
        
        auth.setVerificationCode("123456");
        auth.setCodeExpiryTime(LocalDateTime.now().minusMinutes(10));
        user.setAuthDetails(auth);
        auth.setAuthorities("ROLE_USER");
        userRepository.save(user);

        VerifyUserDto body = new VerifyUserDto("john@mail.com", "username","123456");

        mvc.perform(
                        post("/api/v1/auths/verify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.instance").value("/api/v1/auths/verify"))
                .andExpect(jsonPath("$.errorMessage").value("Verification code expired"));

        assertThat(
                userRepository
                        .findByUsername("john")
                        .orElseThrow()
                        .getAuthDetails()
                        .getIsVerified())
                .isFalse();
    }

    @Test
    @DisplayName("Verify user with invalid code ")
    void verifyUser_invalidCode() throws Exception {
        User user = new User("john", "john@mail.com", passwordEncoder.encode("Password1!"));
        AuthDetails auth = new AuthDetails();
        
        auth.setVerificationCode("123456");
        auth.setCodeExpiryTime(LocalDateTime.now().plusMinutes(10));
        user.setAuthDetails(auth);
        auth.setAuthorities("ROLE_USER");
        userRepository.save(user);

        VerifyUserDto body = new VerifyUserDto("john@mail.com", "username","111111");

        mvc.perform(
                        post("/api/v1/auths/verify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.instance").value("/api/v1/auths/verify"))
                .andExpect(jsonPath("$.errorMessage").value("Verification code is invalid"));

        assertThat(
                userRepository
                        .findByUsername("john")
                        .orElseThrow()
                        .getAuthDetails()
                        .getIsVerified())
                .isFalse();
    }

    /* ============================================================== */
    /* === optional extra flow: forgot-password and reset-password === */
    /* ============================================================== */

    @Nested
    class ForgotPasswordFlow {

        @Test
        @DisplayName("Forgot-password email is sent and password is reset")
        void forgotAndResetPassword() throws Exception {
            /* --- create & verify user --- */
            User user =
                    new User(
                            "jane", "jane@mail.com", passwordEncoder.encode("Password1!"));
            AuthDetails auth = new AuthDetails();
            
            auth.setIsVerified(true);
            auth.setAuthorities("ROLE_USER");
            user.setAuthDetails(auth);
            userRepository.save(user);

            /* ---------- 1) send forgot-password code ---------- */
            mvc.perform(post("/api/v1/auths/forgot/jane")).andExpect(status().isOk());

            verify(emailService)
                    .sendResetPasswordEmail(anyString(), eq("jane@mail.com"));

            // fetch code that service stored
            String code =
                    userRepository
                            .findByUsername("jane")
                            .orElseThrow()
                            .getAuthDetails()
                            .getVerificationCode();

            /* ---------- 2) reset the password ---------- */
            ForgetPasswordDto resetBody =
                    new ForgetPasswordDto("jane", "NewPassword1!", code);

            mvc.perform(
                            put("/api/v1/auths/reset")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(resetBody)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("jane"));

            // old password must now be invalid, new one valid
            String newHash =
                    userRepository.findByUsername("jane").orElseThrow().getPassword();
            assertThat(passwordEncoder.matches("NewPassword1!", newHash)).isTrue();
        }
    }

    /* ============================================================== */
    /* === change-password when logged-in (no security filter here) == */
    /* ============================================================== */

    @Test
    @DisplayName("PUT /api/v1/auths/password → 200 OK + UserDto")
    void changePassword_success() throws Exception {
        User user = new User("bob", "bob@mail.com", passwordEncoder.encode("Password1!"));
        AuthDetails auth = new AuthDetails();
        auth.setIsVerified(true);
        auth.setAuthorities("ROLE_USER");
        user.setAuthDetails(auth);
        userRepository.save(user);

        ChangePasswordDto body = new ChangePasswordDto("bob", "Password1!", "Better2#");

        mvc.perform(
                        put("/api/v1/auths/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"));

        String updatedHash =
                userRepository.findByUsername("bob").orElseThrow().getPassword();
        assertThat(passwordEncoder.matches("Better2#", updatedHash)).isTrue();
    }

    @Test
    @DisplayName("Change password with incorrect old password → 400 BAD REQUEST")
    void changePassword_invalidOldPassword() throws Exception {
        User user = new User("bob", "bob@mail.com", passwordEncoder.encode("Password1!"));
        AuthDetails auth = new AuthDetails();
        
        auth.setIsVerified(true);
        user.setAuthDetails(auth);
        userRepository.save(user);

        ChangePasswordDto body = new ChangePasswordDto("bob", "Password1", "Better2#");

        mvc.perform(
                        put("/api/v1/auths/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Current password is not correct"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auths/password"));

        String updatedHash = userRepository.findByUsername("bob").orElseThrow().getPassword();
        assertThat(passwordEncoder.matches("Better2#", updatedHash)).isFalse();
    }

    @Test
    @DisplayName("Change password with invalid password → 400 BAD REQUEST")
    void changePassword_invalidPassword() throws Exception {
        User user = new User("bob", "bob@mail.com", passwordEncoder.encode("Password1!"));
        AuthDetails auth = new AuthDetails();
        
        auth.setIsVerified(true);
        user.setAuthDetails(auth);
        userRepository.save(user);

        ChangePasswordDto body = new ChangePasswordDto("bob", "Password1!", "Better2");

        mvc.perform(
                        put("/api/v1/auths/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("New password is not valid"))
                .andExpect(jsonPath("$.instance").value("/api/v1/auths/password"));

        String updatedHash = userRepository.findByUsername("bob").orElseThrow().getPassword();
        assertThat(passwordEncoder.matches("Better2#", updatedHash)).isFalse();
    }

}