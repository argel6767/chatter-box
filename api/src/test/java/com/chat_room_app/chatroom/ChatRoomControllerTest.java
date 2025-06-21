package com.chat_room_app.chatroom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.chat_room_app.auth.AuthDetails;
import com.chat_room_app.chatroom.dtos.NewChatDto;
import com.chat_room_app.jwt.JwtUtils;
import com.chat_room_app.users.User;
import com.chat_room_app.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import lombok.extern.java.Log;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional                                          // automatic rollback
@Log
class ChatRoomControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper mapper;

    @Autowired private UserRepository userRepository;
    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private ChatRoomService chatRoomService;

    private MockedStatic<JwtUtils> jwt;

    @BeforeEach void init() {
        jwt = Mockito.mockStatic(JwtUtils.class);
    }
    @AfterEach  void close() {
        jwt.close();
    }

    /* ------------------------------------------------------------------ */
    /* helpers                                                            */
    /* ------------------------------------------------------------------ */
    private String asJson(Object o) throws Exception {
        return mapper.writeValueAsString(o);
    }

    private User user(String username, String mail) {
        User u = new User(username, mail, "Password1!");
        AuthDetails auth = new AuthDetails();
        auth.setIsVerified(true);
        auth.setAuthorities("ROLE_USER");
        u.setAuthDetails(auth);
        return userRepository.save(u);
    }

    private Long chatId(String creator, String name, String... members) {
        NewChatDto dto = new NewChatDto(Set.of(members), name);
        return chatRoomService.createChatRoom(dto, creator).id();
    }

    /* ===================================================================
     * POST /api/v1/chats   (create)
     * =================================================================== */
    @Nested
    class CreateChat {

        @Test
        @DisplayName("Create chat → 201 CREATED")
        @WithMockUser(username = "john")
        void create_ok() throws Exception {
            user("john", "john@mail.com");
            user("jane", "jane@mail.com");

            jwt.when(JwtUtils::getCurrentUserUsername).thenReturn("john");

            NewChatDto body = new NewChatDto(Set.of("john", "jane"), "John & Jane");

            mockMvc
                    .perform(
                            post("/api/v1/chats")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(asJson(body))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("John & Jane"))
                    .andExpect(jsonPath("$.members", hasSize(2)));

            assertThat(chatRoomRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Unauthenticated create → 403 FORBIDDEN")
        void create_forbidden() throws Exception {
            mockMvc
                    .perform(post("/api/v1/chats"))
                    .andExpect(status().isForbidden());
        }
    }

    /* ===================================================================
     * GET /api/v1/chats/{id}
     * =================================================================== */
    @Nested
    class GetChat {

        @Test
        @WithMockUser(username = "john")
        void get_ok() throws Exception {
            user("john", "john@mail.com");
            user("jane", "jane@mail.com");

            Long id = chatId("john", "room", "john", "jane");

            jwt.when(JwtUtils::getCurrentUserUsername).thenReturn("john");

            mockMvc
                    .perform(get("/api/v1/chats/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.members", hasSize(2)));
        }

        @Test
        @WithMockUser(username = "outsider")
        void get_unauthorizedNotAMember() throws Exception {
            user("john", "john@mail.com");
            user("jane", "jane@mail.com");
            user("outsider", "out@mail.com");

            Long id = chatId("john", "room", "john", "jane");

            jwt.when(JwtUtils::getCurrentUserUsername).thenReturn("outsider");

            mockMvc
                    .perform(get("/api/v1/chats/{id}", id))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorMessage").value("User is not a member: outsider"));
        }

        @Test
        @WithMockUser(username = "john")
        void get_notFound() throws Exception {
            user("john", "john@mail.com");

            jwt.when(JwtUtils::getCurrentUserUsername).thenReturn("john");

            mockMvc
                    .perform(get("/api/v1/chats/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorMessage").value("Chat room not found with id: 999"));
        }
    }

    /* ===================================================================
     * DELETE /api/v1/chats/{id}  (owner only)
     * =================================================================== */
    @Nested
    class DeleteChat {

        @Test
        @WithMockUser(username = "john")
        void delete_ok() throws Exception {
            user("john", "john@mail.com");
            Long id = chatId("john", "room", "john");

            jwt.when(JwtUtils::getCurrentUserUsername).thenReturn("john");

            mockMvc.perform(delete("/api/v1/chats/{id}", id))
                    .andExpect(status().isNoContent());

            assertThat(chatRoomRepository.findById(id)).isEmpty();
        }

        @Test
        @WithMockUser(username = "jane")
        void delete_notOwner() throws Exception {
            user("john", "john@mail.com");
            user("jane", "jane@mail.com");
            Long id = chatId("john", "room", "john", "jane");

            jwt.when(JwtUtils::getCurrentUserUsername).thenReturn("jane");

            mockMvc
                    .perform(delete("/api/v1/chats/{id}", id))
                    .andExpect(status().isUnauthorized())
                    .andExpect(
                            jsonPath("$.errorMessage")
                                    .value("Only the chat room owner can delete the chat room")
                    );
        }
    }

    /* ===================================================================
     * PUT /api/v1/chats/{chatId}/members/{username}   (add member)
     * =================================================================== */
    @Nested
    class AddMember {

        @Test
        @WithMockUser(username = "john")
        void add_ok() throws Exception {
            user("john", "john@mail.com");
            user("jane", "jane@mail.com");
            user("alice", "alice@mail.com");

            Long id = chatId("john", "room", "john", "jane");

            jwt.when(JwtUtils::getCurrentUserUsername).thenReturn("john");

            mockMvc
                    .perform(put("/api/v1/chats/{id}/members/{u}", id, "alice"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.members", hasSize(3)));

            assertThat(
                    chatRoomRepository.findById(id).orElseThrow().getMembers()
            ).extracting(User::getUsername).contains("alice");
        }

        @Test
        @WithMockUser(username = "john")
        void add_conflictAlreadyMember() throws Exception {
            user("john", "john@mail.com");
            user("jane", "jane@mail.com");

            Long id = chatId("john", "room", "john", "jane");

            jwt.when(JwtUtils::getCurrentUserUsername).thenReturn("john");

            mockMvc
                    .perform(put("/api/v1/chats/{id}/members/{u}", id, "jane"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorMessage").value("User is already a member: jane"));
        }

        @Test
        @WithMockUser(username = "outsider")
        void add_notAMember() throws Exception {
            user("john", "john@mail.com");
            user("jane", "jane@mail.com");
            user("outsider", "out@mail.com");

            Long id = chatId("john", "room", "john", "jane");

            jwt.when(JwtUtils::getCurrentUserUsername).thenReturn("outsider");

            mockMvc
                    .perform(put("/api/v1/chats/{id}/members/{u}", id, "jane"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorMessage").value("User is not a member: outsider"));
        }
    }

    /* ===================================================================
     * DELETE /api/v1/chats/{id}/members/{username}   (owner kicks member)
     * =================================================================== */
    @Nested
    class RemoveMember {

        @Test
        @WithMockUser(username = "john")
        void remove_ok() throws Exception {
            user("john", "john@mail.com");
            user("jane", "jane@mail.com");

            Long id = chatId("john", "room", "john", "jane");

            jwt.when(JwtUtils::getCurrentUserUsername).thenReturn("john");

            mockMvc
                    .perform(delete("/api/v1/chats/{id}/members/{u}", id, "jane"))
                    .andExpect(status().isNoContent());

            assertThat(
                    chatRoomRepository.findById(id).orElseThrow().getMembers()
            ).extracting(User::getUsername).doesNotContain("jane");
        }

        @Test
        @WithMockUser(username = "jane")
        void remove_notOwner() throws Exception {
            user("john", "john@mail.com");
            user("jane", "jane@mail.com");
            Long id = chatId("john", "room", "john", "jane");

            jwt.when(JwtUtils::getCurrentUserUsername).thenReturn("jane");

            mockMvc
                    .perform(delete("/api/v1/chats/{id}/members/{u}", id, "john"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(
                            jsonPath("$.errorMessage")
                                    .value("Only the chat room owner can remove users from the chat room")
                    );
        }
    }

    /* ===================================================================
     * DELETE /api/v1/chats/{id}/members/me   (leave)
     * =================================================================== */
    @Nested
    class LeaveChat {

        @Test
        @WithMockUser(username = "jane")
        void leave_ok() throws Exception {
            user("john", "john@mail.com");
            user("jane", "jane@mail.com");

            Long id = chatId("john", "room", "john", "jane");

            jwt.when(JwtUtils::getCurrentUserUsername).thenReturn("jane");

            mockMvc
                    .perform(delete("/api/v1/chats/{id}/members/me", id))
                    .andExpect(status().isNoContent());

            assertThat(
                    chatRoomRepository.findById(id).orElseThrow().getMembers()
            ).extracting(User::getUsername).doesNotContain("jane");
        }

        @Test
        void leave_unauthenticated() throws Exception {
            mockMvc
                    .perform(delete("/api/v1/chats/1/members/me"))
                    .andExpect(status().isForbidden());
        }
    }
}