package com.chat_room_app.web_socket;


import lombok.extern.java.Log;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;

@Log
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // On every frame except CONNECT, restore what we saved earlier
        Map<String, Object> attrs = acc.getSessionAttributes();
        if (attrs != null && attrs.containsKey("authenticatedUser")) {
            Principal user = (Principal) attrs.get("authenticatedUser");
            acc.setUser(user);
            // also propagate to the SecurityContext so @PreAuthorize works
            SecurityContextHolder.getContext().setAuthentication((Authentication) user);
        }
        return message;
    }
}