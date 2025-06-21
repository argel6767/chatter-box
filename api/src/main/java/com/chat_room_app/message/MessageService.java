package com.chat_room_app.message;

import com.chat_room_app.chatroom.ChatRoom;
import com.chat_room_app.chatroom.ChatRoomRepository;
import com.chat_room_app.chatroom.ChatRoomService;
import com.chat_room_app.exceptions.NotFound404Exception;
import com.chat_room_app.exceptions.UnAuthorized401Exception;
import com.chat_room_app.message.dtos.MessageDto;
import com.chat_room_app.message.dtos.NewMessageDto;
import com.chat_room_app.message.dtos.UpdateMessageDto;
import lombok.extern.java.Log;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Log
public class MessageService {

    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService chatRoomService;

    public MessageService(MessageRepository messageRepository, SimpMessagingTemplate messagingTemplate, ChatRoomService chatRoomService) {
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
        this.chatRoomService = chatRoomService;
    }

    public void sendMessage(NewMessageDto request, String username) {
        ChatRoom chatRoom = chatRoomService.getChatRoomById(request.chatRoomId());
        if (!chatRoomService.isAMember(username, chatRoom)) {
            throw new UnAuthorized401Exception("User is not part of a chat: " + username);
        }
        Message message = new Message();
        message.setChatRoom(chatRoom);
        message.setContent(request.content());
        message.setSender(username);
        Message savedMessage = messageRepository.save(message);
        MessageDto dto = toMessageDto(savedMessage);

        // Send to a dynamic topic based on chatId
        messagingTemplate.convertAndSend(
                "/topic/chat." + request.chatRoomId(),
                dto
        );
    }

    public void deleteMessage(Long messageId, String username) {
        Message message = getMessageById(messageId);
        if (!message.getSender().equals(username)) {
            log.warning("Message does not belong to user: " + username);
            throw new UnAuthorized401Exception("User is not the original author of message: " + username);
        }
        messageRepository.delete(message);
        messagingTemplate.convertAndSend(
                "/topic/chat." + message.getChatRoom().getId() + ".delete",
                messageId
        );
    }

    public void editMessage(UpdateMessageDto request, String username) {
        Message message = getMessageById(request.messageId());
        if (!message.getSender().equals(username)) {
            throw new UnAuthorized401Exception("User is not the original author of message: " + username);
        }
        message.setContent(request.newContent());
        MessageDto dto = toMessageDto(message);
        messageRepository.save(message);
        messagingTemplate.convertAndSend(
                "/topic/chat." + message.getChatRoom().getId() + ".edit",
                dto
        );
    }

    // Helper method
    private MessageDto toMessageDto(Message message) {
        String time = message.getCreated().toLocalTime().toString();
        return new MessageDto(
                message.getId(),
                message.getContent(),
                message.getSender(),
                time
        );
    }

    private Message getMessageById(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFound404Exception("Message not found with id: " + messageId));
    }
}
