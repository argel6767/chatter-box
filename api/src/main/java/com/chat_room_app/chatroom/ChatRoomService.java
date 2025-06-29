package com.chat_room_app.chatroom;

import com.chat_room_app.chatroom.dtos.ChatRoomDto;
import com.chat_room_app.chatroom.dtos.NewChatDto;
import com.chat_room_app.exceptions.Conflict409Exception;
import com.chat_room_app.exceptions.NotFound404Exception;
import com.chat_room_app.exceptions.UnAuthorized401Exception;
import com.chat_room_app.message.dtos.MessageDto;
import com.chat_room_app.users.User;
import com.chat_room_app.users.UserRepository;
import com.chat_room_app.users.dtos.ChatRoomUserDto;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Set;

@Service
@Log
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository, UserRepository userRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.userRepository = userRepository;
    }

    /**
     * creates a new ChatRoom entity
     * @param request
     * @param creatorUsername
     * @return
     */
    public ChatRoomDto createChatRoom(NewChatDto request, String creatorUsername) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatRoomCreator(creatorUsername);
        List<User> users = userRepository.findAllByUsernameIn(request.usernames());
        chatRoom.getMembers().addAll(users);
        users.forEach(user -> user.getChatRooms().add(chatRoom));
        if (request.name() != null || !(request.name().isEmpty())) {
            chatRoom.setName(request.name());
        }
        log.info("New Chat Room created by user: " + creatorUsername);
        chatRoomRepository.save(chatRoom);
        return createChatRoomDto(chatRoom);
    }

    /**
     * Gets ChatRoom info
     * @param chatRoomId
     * @param username
     * @return
     */
    public ChatRoomDto getChatRoom(Long chatRoomId, String username) {
        log.info("Fetching ChatRoom info: " + chatRoomId);
        ChatRoom chatRoom = getChatRoomById(chatRoomId);
        Set<User> members = chatRoom.getMembers();
        if (!isAMember(username, members)) {
            log.warning("Unauthorized user: " + username + " attempting to enter chat room: " + chatRoom.getName());
            throw new UnAuthorized401Exception("You are not a member of " + chatRoom.getName());
        }
        return createChatRoomDto(chatRoom);
    }

    public boolean isAMember(String username, Long chatRoomId) {
        return chatRoomRepository.existsByIdAndMembersUsername(chatRoomId, username);
    }

    private boolean isAMember(String username, Set<User> members) {
        for (User user : members) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Deletes a ChatRoom, only the owner can delete it
     * @param username
     * @param chatRoomId
     */
    public void deleteChatRoom(Long chatRoomId, String username) {
        ChatRoom chatRoom = getChatRoomById(chatRoomId);
        if (!chatRoom.getChatRoomCreator().equals(username)) {
            log.warning("Non owner tried to delete room: " + username);
            throw new UnAuthorized401Exception("Only the chat room owner can delete the chat room");
        }
        log.info("Deleting chat room: " + chatRoom.getName());
        chatRoomRepository.delete(chatRoom);
    }

    /**
     * adds a new user to a ChatRoom
     * @param username
     * @param chatRoomId
     * @return
     */
    public ChatRoomDto addUserToChatRoom(Long chatRoomId, String username, String requesterUsername) {
        ChatRoom chatRoom = getChatRoomById(chatRoomId);
        User newMember = userRepository.findByUsername(username).orElseThrow(() -> new NotFound404Exception("User not found with username: " + username));
        Set<User> members = chatRoom.getMembers();
        if (!isAMember(requesterUsername, members)) {
            log.warning("Non-member: " + requesterUsername + " tried to add user: " + username +  " to chat room: " + chatRoom.getName());
            throw new UnAuthorized401Exception("User is not a member: " + requesterUsername);
        }
        if (isAMember(username, members)) {
            log.warning("User: " + username + " is already a member of chat room: " + chatRoom.getName());
            throw new Conflict409Exception("User is already a member: " + username);
        }
        chatRoom.getMembers().add(newMember);
        log.info("User: " + username + " added to chat room: " + chatRoom.getName());
        chatRoomRepository.save(chatRoom);
        return createChatRoomDto(chatRoom);
    }

    /**
     * removes a User from a ChatRoom forcefully, only the owner can do this
     * @param username
     * @param chatRoomId
     * @return
     */
    public ChatRoomDto removeUserFromChatRoom(Long chatRoomId, String username, String requesterUsername) {
        ChatRoom chatRoom = getChatRoomById(chatRoomId);
        if (!chatRoom.getChatRoomCreator().equals(requesterUsername)) {
            log.warning("Non-owner attempted to remove user: " + username + " from chat room: " + chatRoom.getName());
            throw new UnAuthorized401Exception("Only the chat room owner can remove users from the chat room");
        }
        leaveChatRoom(chatRoomId, username);
        return createChatRoomDto(chatRoom);
    }

    /**
     * Removes a user from a ChatRoom by their own volition
     * @param username
     * @param chatRoomId
     */
    public void leaveChatRoom(Long chatRoomId, String username) {
        ChatRoom chatRoom = getChatRoomById(chatRoomId);
        chatRoom.getMembers().removeIf(user -> user.getUsername().equals(username));
        log.info("User: " + username + " was removed from chat room: " + chatRoom.getName());
        chatRoomRepository.save(chatRoom);
    }

    //helpers

    public ChatRoom getChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new NotFound404Exception("Chat room not found with id: " + chatRoomId));
    }

    private ChatRoomDto createChatRoomDto(ChatRoom chatRoom) {
        List<ChatRoomUserDto> members = chatRoom.getMembers().stream().map(user ->
                new ChatRoomUserDto(user.getId(), user.getUsername())).toList();
        List<MessageDto> messages = chatRoom.getMessages().stream().map(message -> {
            String time = message.getCreated().toLocalTime().toString();
            return new MessageDto(message.getId(), message.getContent(), message.getSender(), time);
        }).toList();
        return new ChatRoomDto(chatRoom.getId(), chatRoom.getName(), chatRoom.getChatRoomCreator(), members, messages);
    }
}
