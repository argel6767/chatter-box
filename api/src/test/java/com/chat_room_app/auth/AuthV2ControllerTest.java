package com.chat_room_app.auth;

import com.chat_room_app.auth.dtos.RegisterUserDto;
import com.chat_room_app.auth.dtos.VerifyUserDto;
import com.chat_room_app.email.ChatterBoxEmailService;
import com.chat_room_app.users.User;
import com.chat_room_app.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class AuthV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepository;

    // external HTTP layer is mocked
    @MockBean
    private ChatterBoxEmailService emailService;

    // replace the virtual-thread executor with a synchronous one
    @TestConfiguration
    static class SyncExecutorConfig {
        @Bean
        @Qualifier("virtualThreadExecutor")
        Executor sameThreadExecutor() {
            return Runnable::run;
        }
    }

    @BeforeEach
    void cleanDb() {
        userRepository.deleteAll();
    }

    @DisplayName("POST /api/v2/auths/register → 201, user stored, e-mail queued")
    @Test
    void register_happyPath() throws Exception {
        RegisterUserDto dto =
                new RegisterUserDto("john@mail.com", "john", "Secret123*");

        // --- call the endpoint ------------------------------------------------
        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post("/api/v2/auths/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(
                        MockMvcResultMatchers
                                .content()
                                .string("User registered successfully"));

        // --- database assertions ---------------------------------------------
        User saved = userRepository.findByUsername("john").orElseThrow();
        assertThat(saved.getEmail()).isEqualTo("john@mail.com");
        assertThat(saved.getPassword()).isNotEqualTo("secret");   // encoded

        // --- external call was made (mocked) ---------------------------------
        verify(emailService).sendVerifyEmail(any(VerifyUserDto.class));
    }

    @DisplayName("POST /api/v2/auths/resend-verification/{username} → 200")
    @Test
    void resendVerification_happyPath() throws Exception {
        // a not-yet-verified user must already exist
        User user = new User("alice", "alice@mail.com", "pwd");
        AuthDetails authDetails = new AuthDetails();
        authDetails.setIsVerified(false);
        user.setAuthDetails(authDetails);
        userRepository.save(user);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post("/api/v2/auths/resend-verification/{u}",
                                        "alice"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .content()
                                .string("Verification email sent"));

        verify(emailService).sendVerifyEmail(any(VerifyUserDto.class));
    }

    @DisplayName("POST /api/v2/auths/forgot/{username} → 200")
    @Test
    void forgotPassword_happyPath() throws Exception {
        User user = new User("bob", "bob@mail.com", "pwd");
        userRepository.save(user);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post("/api/v2/auths/forgot/{u}", "bob"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .content()
                                .string("Forgot password verification code sent"));

        verify(emailService).sendRestPasswordEmail(any(VerifyUserDto.class));
    }
}