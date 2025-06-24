package com.chat_room_app.users;

import com.chat_room_app.auth.AuthDetails;
import com.chat_room_app.jwt.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@Log
class UserControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;

    private MockedStatic<JwtUtils> jwtUtilsMock;


    private String toJson(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    @BeforeEach
    void setUp() {
        jwtUtilsMock = Mockito.mockStatic(JwtUtils.class);
    }

    @AfterEach
    void tearDown() {
        if (jwtUtilsMock != null) {
            jwtUtilsMock.close();
        }
    }


    @Test
    @DisplayName("GET /api/v1/users/me → 200 OK")
    @WithMockUser(username = "Billy")
    void getUser_authenticated() throws Exception {
        User user = new User("Billy", "billy@email.com", "password");
        AuthDetails authDetails = new AuthDetails();
        authDetails.setIsVerified(true);
        authDetails.setAuthorities("ROLE_USER");
        user.setAuthDetails(authDetails);
        userRepository.save(user);

        jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenReturn(user.getId());


        mockMvc.perform(
                get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Billy"));
    }

    @Test
    @DisplayName("Fetching user while unauthenticated → 403 FORBIDDEN")
    void getUser_unauthenticated() throws Exception {
        User user = new User("Billy", "billy@email.com", "password");
        AuthDetails authDetails = new AuthDetails();
        authDetails.setIsVerified(true);
        authDetails.setAuthorities("ROLE_USER");
        user.setAuthDetails(authDetails);
        userRepository.save(user);

        jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenThrow(IllegalStateException.class);


        mockMvc.perform(
                        get("/api/v1/users/me"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorMessage").value("No authenticated user found"))
                .andExpect(jsonPath("$.instance").value("/api/v1/users/me"));
    }

    @Test
    @DisplayName("DEL /api/v1/users/me → 201 NO CONTENT")
    @WithMockUser(username = "Billy")
    void deleteUser_authenticated() throws Exception {
        User user = new User("Billy", "billy@email.com", "password");
        AuthDetails authDetails = new AuthDetails();
        authDetails.setIsVerified(true);
        authDetails.setAuthorities("ROLE_USER");
        user.setAuthDetails(authDetails);
        userRepository.save(user);

        mockMvc.perform(
                        delete("/api/v1/users/me"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Delete user unauthenticated → 403 FORBIDDEN")
    void deleteUser_unauthenticated() throws Exception {
        User user = new User("Billy", "billy@email.com", "password");
        AuthDetails authDetails = new AuthDetails();
        authDetails.setIsVerified(true);
        authDetails.setAuthorities("ROLE_USER");
        user.setAuthDetails(authDetails);
        userRepository.save(user);

        jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenThrow(IllegalStateException.class);


        mockMvc.perform(
                        delete("/api/v1/users/me"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorMessage").value("No authenticated user found"))
                .andExpect(jsonPath("$.instance").value("/api/v1/users/me"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} → 200 OK with mutual friends and common chat rooms")
    @WithMockUser(username = "John")
    void getUserProfile_authenticated_withMutualFriendsAndChatRooms() throws Exception {
        // Create users
        User requester = new User("John", "john@email.com", "password");
        AuthDetails requesterAuth = new AuthDetails();
        requesterAuth.setIsVerified(true);
        requesterAuth.setAuthorities("ROLE_USER");
        requester.setAuthDetails(requesterAuth);
        userRepository.save(requester);

        User searchedUser = new User("Phil", "phil@email.com", "password");
        AuthDetails searchedAuth = new AuthDetails();
        searchedAuth.setIsVerified(true);
        searchedAuth.setAuthorities("ROLE_USER");
        searchedUser.setAuthDetails(searchedAuth);
        userRepository.save(searchedUser);

        // Create mutual friend
        User mutualFriend = new User("Alice", "alice@email.com", "password");
        AuthDetails mutualFriendAuth = new AuthDetails();
        mutualFriendAuth.setIsVerified(true);
        mutualFriendAuth.setAuthorities("ROLE_USER");
        mutualFriend.setAuthDetails(mutualFriendAuth);
        userRepository.save(mutualFriend);

        jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenReturn(requester.getId());

        mockMvc.perform(
                        get("/api/v1/users/{searchUserId}", searchedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Phil"))
                .andExpect(jsonPath("$.friends").isArray())
                .andExpect(jsonPath("$.commonChatRooms").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} → 200 OK with empty friends and chat rooms")
    @WithMockUser(username = "John")
    void getUserProfile_authenticated_withoutMutualFriendsOrChatRooms() throws Exception {
        // Create users
        User requester = new User("John", "john@email.com", "password");
        AuthDetails requesterAuth = new AuthDetails();
        requesterAuth.setIsVerified(true);
        requesterAuth.setAuthorities("ROLE_USER");
        requester.setAuthDetails(requesterAuth);
        userRepository.save(requester);

        User searchedUser = new User("Phil", "phil@email.com", "password");
        AuthDetails searchedAuth = new AuthDetails();
        searchedAuth.setIsVerified(true);
        searchedAuth.setAuthorities("ROLE_USER");
        searchedUser.setAuthDetails(searchedAuth);
        userRepository.save(searchedUser);

        jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenReturn(requester.getId());


        mockMvc.perform(
                        get("/api/v1/users/{searchUserId}", searchedUser.getId()))
                .andExpect(jsonPath("$.username").value("Phil"))
                .andExpect(jsonPath("$.friends").isEmpty())
                .andExpect(jsonPath("$.commonChatRooms").isEmpty())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} → 404 NOT FOUND when user doesn't exist")
    @WithMockUser(username = "John")
    void getUserProfile_authenticated_userNotFound() throws Exception {
        // Create requester
        User requester = new User("John", "john@email.com", "password");
        AuthDetails requesterAuth = new AuthDetails();
        requesterAuth.setIsVerified(true);
        requesterAuth.setAuthorities("ROLE_USER");
        requester.setAuthDetails(requesterAuth);
        userRepository.save(requester);

        Long nonExistentUserId = 999L;

        jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenReturn(requester.getId());


        mockMvc.perform(
                        get("/api/v1/users/{searchUserId}", nonExistentUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.instance").value("/api/v1/users/" + nonExistentUserId));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} → 403 FORBIDDEN when unauthenticated")
    void getUserProfile_unauthenticated() throws Exception {
        // Create searched user
        User searchedUser = new User("Phil", "phil@email.com", "password");
        AuthDetails searchedAuth = new AuthDetails();
        searchedAuth.setIsVerified(true);
        searchedAuth.setAuthorities("ROLE_USER");
        searchedUser.setAuthDetails(searchedAuth);
        userRepository.save(searchedUser);

        jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenThrow(IllegalStateException.class);


        mockMvc.perform(
                        get("/api/v1/users/{searchUserId}", searchedUser.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorMessage").isEmpty())
                .andExpect(jsonPath("$.instance").value("/api/v1/users/" + searchedUser.getId()));
    }

    @Test
    @DisplayName("GET /api/v1/users/invalid → 400 BAD REQUEST for invalid path variable")
    @WithMockUser(username = "John")
    void getUserProfile_invalidPathVariable() throws Exception {
        // Create requester
        User requester = new User("John", "john@email.com", "password");
        AuthDetails requesterAuth = new AuthDetails();
        requesterAuth.setIsVerified(true);
        requesterAuth.setAuthorities("ROLE_USER");
        requester.setAuthDetails(requesterAuth);
        userRepository.save(requester);

        mockMvc.perform(
                        get("/api/v1/users/invalid"))
                .andExpect(status().isBadRequest());
    }



}