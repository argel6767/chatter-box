package com.chat_room_app.message;

import com.chat_room_app.jwt.JwtUtils;
import com.chat_room_app.message.dtos.DeleteMessageDto;
import com.chat_room_app.message.dtos.NewMessageDto;
import com.chat_room_app.message.dtos.UpdateMessageDto;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(NewMessageDto request) {
        String username = JwtUtils.getCurrentUserUsername();
        messageService.sendMessage(request, username);
    }

    @MessageMapping("/chat.deleteMessage")
    public void deleteMessage(DeleteMessageDto request) {
        String username = JwtUtils.getCurrentUserUsername();
        messageService.deleteMessage(request.messageId(), username);
    }

    @MessageMapping("/chat.editMessage")
    public void editMessage(UpdateMessageDto request) {
        String username = JwtUtils.getCurrentUserUsername();
        messageService.editMessage(request, username);
    }

}
