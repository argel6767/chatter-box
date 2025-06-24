package com.chat_room_app.friends;

import com.chat_room_app.auth.AuthDetails;
import com.chat_room_app.jwt.JwtUtils;
import com.chat_room_app.users.User;
import com.chat_room_app.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@Log
class FriendshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    private MockedStatic<JwtUtils> jwtUtilsMock;

    @BeforeEach
    void setUp() {
        jwtUtilsMock = Mockito.mockStatic(JwtUtils.class);
    }

    @AfterEach
    void tearDown() {
        jwtUtilsMock.close();
    }

    private String toJson(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    private User createVerifiedUser(String username, String email) {
        User user = new User(username, email, "Password1!");
        AuthDetails authDetails = new AuthDetails();
        authDetails.setIsVerified(true);
        authDetails.setAuthorities("ROLE_USER");
        user.setAuthDetails(authDetails);
        return userRepository.save(user);
    }

    /* ==================================================================
     * POST /api/v1/friends/request/{friendId}
     * ================================================================== */
    @Nested
    @DisplayName("POST /api/v1/friends/request/{friendId}")
    class RequestFriendship {

        @Test
        @WithMockUser(username = "john")
        @DisplayName("Successfully create friend request → 201 CREATED")
        void created() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");
            User jane = createVerifiedUser("jane", "jane@email.com");

            jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenReturn(john.getId());

            mockMvc
                    .perform(post("/api/v1/friends/request/{id}", jane.getId()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.user.id").value(john.getId()))
                    .andExpect(jsonPath("$.user.username").value("john"))
                    .andExpect(jsonPath("$.friend.id").value(jane.getId()))
                    .andExpect(jsonPath("$.friend.username").value("jane"))
                    .andExpect(jsonPath("$.status").value("PENDING"));

            // Verify friendship was created in database
            assertThat(friendshipRepository.findByRequesterIdAndReceiverId(john.getId(), jane.getId()))
                    .isPresent()
                    .get()
                    .satisfies(friendship -> {
                        assertThat(friendship.getStatus()).isEqualTo(FriendStatus.PENDING);
                        assertThat(friendship.getRequester().getUsername()).isEqualTo("john");
                        assertThat(friendship.getReceiver().getUsername()).isEqualTo("jane");
                    });
        }

        @Test
        @WithMockUser(username = "john")
        @DisplayName("Request friendship to self → 409 CONFLICT")
        void cannotRequestSelf() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");

            jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenReturn(john.getId());

            mockMvc
                    .perform(post("/api/v1/friends/request/{id}", john.getId()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorMessage").value("Cannot send friend request to yourself"))
                    .andExpect(jsonPath("$.instance").value("/api/v1/friends/request/" + john.getId()));
        }

        @Test
        @WithMockUser(username = "john")
        @DisplayName("Request friendship that already exists → 409 CONFLICT")
        void duplicateRequest() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");
            User jane = createVerifiedUser("jane", "jane@email.com");

            // Create existing friendship
            Friendship existing = new Friendship();
            existing.setRequester(john);
            existing.setReceiver(jane);
            existing.setStatus(FriendStatus.PENDING);
            friendshipRepository.save(existing);

            jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenReturn(john.getId());

            mockMvc
                    .perform(post("/api/v1/friends/request/{id}", jane.getId()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorMessage").value("Friendship already exists"))
                    .andExpect(jsonPath("$.instance").value("/api/v1/friends/request/" + jane.getId()));
        }

        @Test
        @WithMockUser(username = "john")
        @DisplayName("Request friendship to non-existent user → 404 NOT FOUND")
        void userNotFound() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");

            jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenReturn(john.getId());

            mockMvc
                    .perform(post("/api/v1/friends/request/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorMessage").value("User not found with id: 999"))
                    .andExpect(jsonPath("$.instance").value("/api/v1/friends/request/999"));
        }

        @Test
        @DisplayName("Unauthenticated request → 403 FORBIDDEN")
        void forbiddenWhenAnonymous() throws Exception {
            jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenThrow(new IllegalStateException());
            mockMvc
                    .perform(post("/api/v1/friends/request/5"))
                    .andExpect(status().isForbidden());
        }
    }

    /* ==================================================================
     * PUT /api/v1/friends/accept/{friendshipId}
     * ================================================================== */
    @Nested
    @DisplayName("PUT /api/v1/friends/accept/{friendshipId}")
    class AcceptFriendship {

        @Test
        @WithMockUser(username = "jane")
        @DisplayName("Successfully accept friend request → 200 OK")
        void acceptSuccess() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");
            User jane = createVerifiedUser("jane", "jane@email.com");

            // Create pending friendship
            Friendship friendship = new Friendship();
            friendship.setRequester(john);
            friendship.setReceiver(jane);
            friendship.setStatus(FriendStatus.PENDING);
            friendship = friendshipRepository.save(friendship);

            mockMvc
                    .perform(put("/api/v1/friends/accept/{id}", friendship.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user.id").value(jane.getId()))
                    .andExpect(jsonPath("$.friend.id").value(john.getId()))
                    .andExpect(jsonPath("$.status").value("ACCEPTED"));

            // Verify status was updated in database
            Friendship updated = friendshipRepository.findById(friendship.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(FriendStatus.ACCEPTED);
        }

        @Test
        @WithMockUser(username = "john")
        @DisplayName("Accept non-existent friendship → 404 NOT FOUND")
        void friendshipNotFound() throws Exception {
            createVerifiedUser("john", "john@email.com");

            mockMvc
                    .perform(put("/api/v1/friends/accept/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorMessage").value("Friendship not found with id: 999"))
                    .andExpect(jsonPath("$.instance").value("/api/v1/friends/accept/999"));
        }

        @Test
        @DisplayName("Unauthenticated accept → 403 FORBIDDEN")
        void forbiddenWhenAnonymous() throws Exception {
            jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenThrow(new IllegalStateException());
            mockMvc
                    .perform(put("/api/v1/friends/accept/1"))
                    .andExpect(status().isForbidden());
        }
    }

    /* ==================================================================
     * DELETE /api/v1/friends/remove/{friendshipId}
     * ================================================================== */
    @Nested
    @DisplayName("DELETE /api/v1/friends/remove/{friendshipId}")
    class RemoveFriendship {

        @Test
        @WithMockUser(username = "john")
        @DisplayName("Successfully remove friendship → 204 NO CONTENT")
        void removeSuccess() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");
            User jane = createVerifiedUser("jane", "jane@email.com");

            // Create accepted friendship
            Friendship friendship = new Friendship();
            friendship.setRequester(john);
            friendship.setReceiver(jane);
            friendship.setStatus(FriendStatus.ACCEPTED);
            friendship = friendshipRepository.save(friendship);

            mockMvc
                    .perform(delete("/api/v1/friends/remove/{id}", friendship.getId()))
                    .andExpect(status().isNoContent());

            // Verify friendship was deleted from database
            assertThat(friendshipRepository.findById(friendship.getId())).isEmpty();
        }

        @Test
        @WithMockUser(username = "john")
        @DisplayName("Remove non-existent friendship → 404 NOT FOUND")
        void friendshipNotFound() throws Exception {
            createVerifiedUser("john", "john@email.com");

            mockMvc
                    .perform(delete("/api/v1/friends/remove/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorMessage").value("Friendship not found with id: 999"))
                    .andExpect(jsonPath("$.instance").value("/api/v1/friends/remove/999"));
        }

        @Test
        @DisplayName("Unauthenticated remove → 403 FORBIDDEN")
        void forbiddenWhenAnonymous() throws Exception {
            mockMvc
                    .perform(delete("/api/v1/friends/remove/1"))
                    .andExpect(status().isForbidden());
        }
    }

    /* ==================================================================
     * POST /api/v1/friends/block/{friendId}
     * ================================================================== */
    @Nested
    @DisplayName("POST /api/v1/friends/block/{friendId}")
    class BlockFriendship {

        @Test
        @WithMockUser(username = "john")
        @DisplayName("Successfully block user → 201 CREATED")
        void blockSuccess() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");
            User jane = createVerifiedUser("jane", "jane@email.com");

            jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenReturn(john.getId());

            mockMvc
                    .perform(post("/api/v1/friends/block/{id}", jane.getId()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.user.id").value(john.getId()))
                    .andExpect(jsonPath("$.friend.id").value(jane.getId()))
                    .andExpect(jsonPath("$.status").value("BLOCKED"));

            // Verify block was created in database
            assertThat(friendshipRepository.findByRequesterIdAndReceiverId(john.getId(), jane.getId()))
                    .isPresent()
                    .get()
                    .satisfies(friendship -> {
                        assertThat(friendship.getStatus()).isEqualTo(FriendStatus.BLOCKED);
                    });
        }

        @Test
        @WithMockUser(username = "john")
        @DisplayName("Block replaces existing friendship")
        void blockReplacesExistingFriendship() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");
            User jane = createVerifiedUser("jane", "jane@email.com");

            // Create existing accepted friendship
            Friendship existing = new Friendship();
            existing.setRequester(john);
            existing.setReceiver(jane);
            existing.setStatus(FriendStatus.ACCEPTED);
            friendshipRepository.save(existing);

            jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenReturn(john.getId());

            mockMvc
                    .perform(post("/api/v1/friends/block/{id}", jane.getId()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("BLOCKED"));

            // Verify old friendship was removed and new block was created
            assertThat(friendshipRepository.findAllByRequesterAndStatus(john, FriendStatus.ACCEPTED))
                    .isEmpty();
            assertThat(friendshipRepository.findAllByRequesterAndStatus(john, FriendStatus.BLOCKED))
                    .hasSize(1);
        }

        @Test
        @WithMockUser(username = "john")
        @DisplayName("Block self → 409 CONFLICT")
        void cannotBlockSelf() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");

            jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenReturn(john.getId());

            mockMvc
                    .perform(post("/api/v1/friends/block/{id}", john.getId()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorMessage").value("Cannot block yourself"))
                    .andExpect(jsonPath("$.instance").value("/api/v1/friends/block/" + john.getId()));
        }

        @Test
        @DisplayName("Unauthenticated block → 403 FORBIDDEN")
        void forbiddenWhenAnonymous() throws Exception {
            mockMvc
                    .perform(post("/api/v1/friends/block/1"))
                    .andExpect(status().isForbidden());
        }
    }

    /* ==================================================================
     * GET collection endpoints
     * ================================================================== */
    @Nested
    @DisplayName("GET collections")
    class GetCollections {

        @Test
        @WithMockUser(username = "john")
        @DisplayName("Get friends returns accepted friendships → 200 OK")
        void getFriends() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");
            User jane = createVerifiedUser("jane", "jane@email.com");
            User bob = createVerifiedUser("bob", "bob@email.com");

            // Create accepted friendships (both directions)
            Friendship friendship1 = new Friendship();
            friendship1.setRequester(john);
            friendship1.setReceiver(jane);
            friendship1.setStatus(FriendStatus.ACCEPTED);
            friendshipRepository.save(friendship1);

            Friendship friendship2 = new Friendship();
            friendship2.setRequester(bob);
            friendship2.setReceiver(john);
            friendship2.setStatus(FriendStatus.ACCEPTED);
            friendshipRepository.save(friendship2);

            // Create pending friendship (should not appear)
            Friendship pending = new Friendship();
            pending.setRequester(john);
            pending.setReceiver(createVerifiedUser("alice", "alice@email.com"));
            pending.setStatus(FriendStatus.PENDING);
            friendshipRepository.save(pending);

            jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenReturn(john.getId());

            mockMvc
                    .perform(get("/api/v1/friends"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].status").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("ACCEPTED"))));
        }

        @Test
        @WithMockUser(username = "jane")
        @DisplayName("Get friend requests returns pending requests → 200 OK")
        void getFriendRequests() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");
            User jane = createVerifiedUser("jane", "jane@email.com");
            User bob = createVerifiedUser("bob", "bob@email.com");

            // Create pending request TO jane
            Friendship pendingToJane = new Friendship();
            pendingToJane.setRequester(john);
            pendingToJane.setReceiver(jane);
            pendingToJane.setStatus(FriendStatus.PENDING);
            friendshipRepository.save(pendingToJane);

            // Create pending request FROM jane (should not appear)
            Friendship pendingFromJane = new Friendship();
            pendingFromJane.setRequester(jane);
            pendingFromJane.setReceiver(bob);
            pendingFromJane.setStatus(FriendStatus.PENDING);
            friendshipRepository.save(pendingFromJane);

            jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenReturn(jane.getId());

            mockMvc
                    .perform(get("/api/v1/friends/requests"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].status").value("PENDING"))
                    .andExpect(jsonPath("$[0].friend.username").value("john")); // The requester
        }

        @Test
        @WithMockUser(username = "john")
        @DisplayName("Get blocked users returns blocked users → 200 OK")
        void getBlockedUsers() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");
            User jane = createVerifiedUser("jane", "jane@email.com");
            User bob = createVerifiedUser("bob", "bob@email.com");

            // Create blocked friendship
            Friendship blocked = new Friendship();
            blocked.setRequester(john);
            blocked.setReceiver(jane);
            blocked.setStatus(FriendStatus.BLOCKED);
            friendshipRepository.save(blocked);

            // Create accepted friendship (should not appear)
            Friendship accepted = new Friendship();
            accepted.setRequester(john);
            accepted.setReceiver(bob);
            accepted.setStatus(FriendStatus.ACCEPTED);
            friendshipRepository.save(accepted);

            jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenReturn(john.getId());

            mockMvc
                    .perform(get("/api/v1/friends/blocked"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].status").value("BLOCKED"))
                    .andExpect(jsonPath("$[0].friend.username").value("jane"));
        }

        @Test
        @DisplayName("Get friends unauthenticated → 403 FORBIDDEN")
        void getFriendsUnauthenticated() throws Exception {
            jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenThrow(new IllegalStateException());
            mockMvc
                    .perform(get("/api/v1/friends/friends"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Get requests unauthenticated → 403 FORBIDDEN")
        void getRequestsUnauthenticated() throws Exception {
            jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenThrow(new IllegalStateException());
            mockMvc
                    .perform(get("/api/v1/friends/requests"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Get blocked unauthenticated → 403 FORBIDDEN")
        void getBlockedUnauthenticated() throws Exception {
            jwtUtilsMock.when(JwtUtils::getCurrentUserId).thenThrow(new IllegalStateException());
            mockMvc
                    .perform(get("/api/v1/friends/blocked"))
                    .andExpect(status().isForbidden());
        }
    }
}