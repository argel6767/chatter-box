package com.chat_room_app.message;

import static org.assertj.core.api.Assertions.assertThat;

import com.chat_room_app.auth.AuthDetails;
import com.chat_room_app.chatroom.ChatRoom;
import com.chat_room_app.chatroom.ChatRoomRepository;
import com.chat_room_app.jwt.JwtUtils;
import com.chat_room_app.message.dtos.DeleteMessageDto;
import com.chat_room_app.message.dtos.MessageDto;
import com.chat_room_app.message.dtos.NewMessageDto;
import com.chat_room_app.message.dtos.UpdateMessageDto;
import com.chat_room_app.users.User;
import com.chat_room_app.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.java.Log;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import com.chat_room_app.web_socket.WebSocketConfigurationTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Import(WebSocketConfigurationTest.class)
@Transactional
@Log
class MessageControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private MessageRepository messageRepository;

    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private MockedStatic<JwtUtils> jwtUtilsMock;

    private String getWsUrl() {
        return "ws://localhost:" + port + "/ws";
    }

    @BeforeEach
    void setUp() throws Exception {
        jwtUtilsMock = Mockito.mockStatic(JwtUtils.class);

        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        stompSession = stompClient.connect(getWsUrl(), new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);
    }

    @AfterEach
    void tearDown() {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
        }
        jwtUtilsMock.close();
    }

    private User createVerifiedUser(String username, String email) {
        User user = new User(username, email, "Password1!");
        AuthDetails authDetails = new AuthDetails();
        authDetails.setIsVerified(true);
        authDetails.setAuthorities("ROLE_USER");
        user.setAuthDetails(authDetails);
        return userRepository.save(user);
    }

    private ChatRoom createChatRoom(String name, User owner, User... members) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(name);
        chatRoom.setChatRoomCreator(owner.getUsername());
        chatRoom.setMembers(Set.of(members));
        return chatRoomRepository.save(chatRoom);
    }

    private Message createMessage(ChatRoom chatRoom, String sender, String content) {
        Message message = new Message();
        message.setChatRoom(chatRoom);
        message.setSender(sender);
        message.setContent(content);
        return messageRepository.save(message);
    }

    /* ==================================================================
     * /chat.sendMessage
     * ================================================================== */
    @Nested
    @DisplayName("/chat.sendMessage")
    class SendMessage {

        @Test
        @DisplayName("Successfully send message → message broadcast to topic")
        @WithMockUser(username = "john")
        void sendMessageSuccess() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");
            User jane = createVerifiedUser("jane", "jane@email.com");
            ChatRoom chatRoom = createChatRoom("Test Chat", john, john, jane);

            jwtUtilsMock.when(JwtUtils::getCurrentUserUsername).thenReturn("john");

            // Subscribe to the chat topic
            CompletableFuture<Object> messageReceived = new CompletableFuture<>();
            stompSession.subscribe("/topic/chat." + chatRoom.getId(), new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Object.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    messageReceived.complete(payload);
                }
            });

            // Send message
            NewMessageDto messageDto = new NewMessageDto( "Hello World!", chatRoom.getId());
            stompSession.send("/app/chat.sendMessage", messageDto);

            // Verify message was received
            Object receivedMessage = messageReceived.get(5, TimeUnit.SECONDS);
            assertThat(receivedMessage).isNotNull();
            assertThat(receivedMessage).isInstanceOf(MessageDto.class);
            MessageDto receivedMessageDto = (MessageDto) receivedMessage;

            // Verify message was saved to database
            assertThat(messageRepository.findById(receivedMessageDto.id()))
                    .isPresent()
                    .satisfies(message -> {
                        assertThat(message.get().getContent()).isEqualTo("Hello World!");
                        assertThat(message.get().getSender()).isEqualTo("john");
                        assertThat(message.get().getChatRoom().getId()).isEqualTo(chatRoom.getId());
                    });
        }

        @Test
        @DisplayName("Send message to non-member chat → no message sent")
        void sendMessageNotMember() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");
            User jane = createVerifiedUser("jane", "jane@email.com");
            User outsider = createVerifiedUser("outsider", "outsider@email.com");
            ChatRoom chatRoom = createChatRoom("Test Chat", john, john, jane);

            jwtUtilsMock.when(JwtUtils::getCurrentUserUsername).thenReturn("outsider");

            // Subscribe to the chat topic
            CompletableFuture<Object> messageReceived = new CompletableFuture<>();
            stompSession.subscribe("/topic/chat." + chatRoom.getId(), new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Object.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    messageReceived.complete(payload);
                }
            });

            // Send message
            NewMessageDto messageDto = new NewMessageDto("Hello World!", chatRoom.getId());
            stompSession.send("/app/chat.sendMessage", messageDto);

            // Verify no message was received (should timeout)
            try {
                messageReceived.get(2, TimeUnit.SECONDS);
                Assertions.fail("Should not have received message");
            } catch (Exception e) {
                // Expected - no message should be sent
            }
        }
    }

    /* ==================================================================
     * /chat.deleteMessage
     * ================================================================== */
    @Nested
    @DisplayName("/chat.deleteMessage")
    class DeleteMessage {

        @Test
        @DisplayName("Successfully delete own message → delete notification sent")
        void deleteMessageSuccess() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");
            User jane = createVerifiedUser("jane", "jane@email.com");
            ChatRoom chatRoom = createChatRoom("Test Chat", john, john, jane);
            Message message = createMessage(chatRoom, "john", "Hello World!");

            jwtUtilsMock.when(JwtUtils::getCurrentUserUsername).thenReturn("john");

            // Subscribe to the delete topic
            CompletableFuture<Object> deleteReceived = new CompletableFuture<>();
            stompSession.subscribe("/topic/chat." + chatRoom.getId() + ".delete", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Object.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    deleteReceived.complete(payload);
                }
            });

            // Delete message
            DeleteMessageDto deleteDto = new DeleteMessageDto(message.getId());
            stompSession.send("/app/chat.deleteMessage", deleteDto);

            // Verify delete notification was received
            Object receivedDelete = deleteReceived.get(5, TimeUnit.SECONDS);
            assertThat(receivedDelete).isNotNull();

            // Verify message was deleted from database
            assertThat(messageRepository.findById(message.getId())).isEmpty();
        }

        @Test
        @DisplayName("Delete message not owned → no deletion occurs")
        void deleteMessageNotOwner() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");
            User jane = createVerifiedUser("jane", "jane@email.com");
            ChatRoom chatRoom = createChatRoom("Test Chat", john, john, jane);
            Message message = createMessage(chatRoom, "john", "Hello World!");

            jwtUtilsMock.when(JwtUtils::getCurrentUserUsername).thenReturn("jane");

            // Subscribe to the delete topic
            CompletableFuture<Object> deleteReceived = new CompletableFuture<>();
            stompSession.subscribe("/topic/chat." + chatRoom.getId() + ".delete", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Object.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    deleteReceived.complete(payload);
                }
            });

            // Try to delete message
            DeleteMessageDto deleteDto = new DeleteMessageDto(message.getId());
            stompSession.send("/app/chat.deleteMessage", deleteDto);

            // Verify no delete notification was received
            try {
                deleteReceived.get(2, TimeUnit.SECONDS);
                Assertions.fail("Should not have received delete notification");
            } catch (Exception e) {
                // Expected - no delete should occur
            }

            // Verify message still exists in database
            assertThat(messageRepository.findById(message.getId())).isPresent();
        }
    }

    /* ==================================================================
     * /chat.editMessage
     * ================================================================== */
    @Nested
    @DisplayName("/chat.editMessage")
    class EditMessage {

        @Test
        @DisplayName("Successfully edit own message → edit notification sent and DB updated")
        void editMessageSuccess() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");
            User jane = createVerifiedUser("jane", "jane@email.com");
            ChatRoom chatRoom = createChatRoom("Test Chat", john, john, jane);
            Message message = createMessage(chatRoom, "john", "Hello World!");

            jwtUtilsMock.when(JwtUtils::getCurrentUserUsername).thenReturn("john");

            // Subscribe to the edit topic
            CompletableFuture<Object> editReceived = new CompletableFuture<>();
            stompSession.subscribe("/topic/chat." + chatRoom.getId() + ".edit", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Object.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    editReceived.complete(payload);
                }
            });

            // Edit message
            UpdateMessageDto updateDto = new UpdateMessageDto(message.getId(), "Hello Universe!");
            stompSession.send("/app/chat.editMessage", updateDto);

            // Verify edit notification was received
            Object receivedEdit = editReceived.get(5, TimeUnit.SECONDS);
            assertThat(receivedEdit).isNotNull();

            // Verify message was updated in database
            Message updatedMessage = messageRepository.findById(message.getId()).orElseThrow();
            assertThat(updatedMessage.getContent()).isEqualTo("Hello Universe!");
            assertThat(updatedMessage.getSender()).isEqualTo("john");
            assertThat(updatedMessage.getChatRoom().getId()).isEqualTo(chatRoom.getId());
        }

        @Test
        @DisplayName("Edit message not owned → no edit occurs")
        void editMessageNotOwner() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");
            User jane = createVerifiedUser("jane", "jane@email.com");
            ChatRoom chatRoom = createChatRoom("Test Chat", john, john, jane);
            Message message = createMessage(chatRoom, "john", "Hello World!");

            jwtUtilsMock.when(JwtUtils::getCurrentUserUsername).thenReturn("jane");

            // Subscribe to the edit topic
            CompletableFuture<Object> editReceived = new CompletableFuture<>();
            stompSession.subscribe("/topic/chat." + chatRoom.getId() + ".edit", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Object.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    editReceived.complete(payload);
                }
            });

            // Try to edit message
            UpdateMessageDto updateDto = new UpdateMessageDto(message.getId(), "Hello Universe!");
            stompSession.send("/app/chat.editMessage", updateDto);

            // Verify no edit notification was received
            try {
                editReceived.get(2, TimeUnit.SECONDS);
                Assertions.fail("Should not have received edit notification");
            } catch (Exception e) {
                // Expected - no edit should occur
            }

            // Verify original message content unchanged in database
            Message originalMessage = messageRepository.findById(message.getId()).orElseThrow();
            assertThat(originalMessage.getContent()).isEqualTo("Hello World!");
        }

        @Test
        @DisplayName("Edit non-existent message → no edit occurs")
        void editNonExistentMessage() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");
            ChatRoom chatRoom = createChatRoom("Test Chat", john, john);

            jwtUtilsMock.when(JwtUtils::getCurrentUserUsername).thenReturn("john");

            // Subscribe to the edit topic
            CompletableFuture<Object> editReceived = new CompletableFuture<>();
            stompSession.subscribe("/topic/chat." + chatRoom.getId() + ".edit", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Object.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    editReceived.complete(payload);
                }
            });

            // Try to edit non-existent message
            UpdateMessageDto updateDto = new UpdateMessageDto(999L, "Hello Universe!");
            stompSession.send("/app/chat.editMessage", updateDto);

            // Verify no edit notification was received
            try {
                editReceived.get(2, TimeUnit.SECONDS);
                Assertions.fail("Should not have received edit notification");
            } catch (Exception e) {
                // Expected - no edit should occur
            }
        }

        @Test
        @DisplayName("Edit message with empty content → message updated in DB")
        void editMessageEmptyContent() throws Exception {
            User john = createVerifiedUser("john", "john@email.com");
            ChatRoom chatRoom = createChatRoom("Test Chat", john, john);
            Message message = createMessage(chatRoom, "john", "Hello World!");

            jwtUtilsMock.when(JwtUtils::getCurrentUserUsername).thenReturn("john");

            // Subscribe to the edit topic
            CompletableFuture<Object> editReceived = new CompletableFuture<>();
            stompSession.subscribe("/topic/chat." + chatRoom.getId() + ".edit", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Object.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    editReceived.complete(payload);
                }
            });

            // Edit message with empty content
            UpdateMessageDto updateDto = new UpdateMessageDto(message.getId(), "");
            stompSession.send("/app/chat.editMessage", updateDto);

            // Verify edit notification was received
            Object receivedEdit = editReceived.get(5, TimeUnit.SECONDS);
            assertThat(receivedEdit).isNotNull();

            // Verify message was updated in database with empty content
            Message updatedMessage = messageRepository.findById(message.getId()).orElseThrow();
            assertThat(updatedMessage.getContent()).isEqualTo("");
            assertThat(updatedMessage.getSender()).isEqualTo("john");
        }
    }
}