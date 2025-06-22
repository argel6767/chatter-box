package com.chat_room_app.message;

import com.chat_room_app.jwt.JwtUtils;
import com.chat_room_app.message.dtos.DeleteMessageDto;
import com.chat_room_app.message.dtos.NewMessageDto;
import com.chat_room_app.message.dtos.UpdateMessageDto;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(NewMessageDto request, Principal principal) {
        String username = principal.getName();
        messageService.sendMessage(request, username);
    }

    @MessageMapping("/chat.deleteMessage")
    public void deleteMessage(DeleteMessageDto request, Principal principal) {
        String username = principal.getName();
        messageService.deleteMessage(request.messageId(), username);
    }

    @MessageMapping("/chat.editMessage")
    public void editMessage(UpdateMessageDto request, Principal principal) {
        String username = principal.getName();
        messageService.editMessage(request, username);
    }

}
