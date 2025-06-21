package com.chat_room_app.chatroom;

import com.chat_room_app.chatroom.dtos.ChatRoomDto;
import com.chat_room_app.chatroom.dtos.NewChatDto;
import com.chat_room_app.jwt.JwtUtils;
import com.chat_room_app.message.dtos.MessageDto;
import com.chat_room_app.users.User;
import com.chat_room_app.users.UserRepository;
import com.chat_room_app.users.dtos.ChatRoomUserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController()
@RequestMapping("api/v1/chats")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    /**
     * Make new ChatRoom
     * @param request
     * @return
     */
    @PostMapping()
    public ResponseEntity<ChatRoomDto> create(@RequestBody NewChatDto request) {
        String username = JwtUtils.getCurrentUserUsername();
        ChatRoomDto dto = chatRoomService.createChatRoom(request, username);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    /**
     * Get ChatRoom details
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChatRoomDto> get(@PathVariable Long id) {
        String username = JwtUtils.getCurrentUserUsername();
        ChatRoomDto dto = chatRoomService.getChatRoom(id, username);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    /**
     * Delete ChatRoom
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        String username = JwtUtils.getCurrentUserUsername();
        chatRoomService.deleteChatRoom(id, username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Add new member to ChatRoom
     * @param chatRoomId
     * @param username
     * @return
     */
    @PutMapping("/{chatRoomId}/members/{username}")
    public ResponseEntity<ChatRoomDto> addMember(@PathVariable Long chatRoomId, @PathVariable String username) {
        String requesterUsername = JwtUtils.getCurrentUserUsername();
        ChatRoomDto dto = chatRoomService.addUserToChatRoom(chatRoomId, username, requesterUsername);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    /**
     * Remove member from ChatRoom
     * @param chatRoomId
     * @param username
     * @return
     */
    @DeleteMapping("/{chatRoomId}/members/{username}")
    public ResponseEntity<ChatRoomDto> removeMember(@PathVariable Long chatRoomId, @PathVariable String username) {
        String requesterUsername = JwtUtils.getCurrentUserUsername();
        ChatRoomDto dto = chatRoomService.removeUserFromChatRoom(chatRoomId, username, requesterUsername);
        return new ResponseEntity<>(dto, HttpStatus.NO_CONTENT);
    }

    /**
     * Leave ChatRoom
     * @param chatRoomId
     * @return
     */
    @DeleteMapping("/{chatRoomId}/members/me")
    public ResponseEntity<Void> leave(@PathVariable Long chatRoomId) {
        String username = JwtUtils.getCurrentUserUsername();
        chatRoomService.leaveChatRoom(chatRoomId, username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
