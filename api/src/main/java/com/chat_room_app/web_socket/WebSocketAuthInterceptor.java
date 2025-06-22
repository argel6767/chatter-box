package com.chat_room_app.web_socket;

import com.chat_room_app.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Extract cookies from native headers
            List<String> cookies = accessor.getNativeHeader("cookie");
            if (cookies != null && !cookies.isEmpty()) {
                String cookieHeader = cookies.get(0);
                String token = extractJwtFromCookies(cookieHeader);

                if (token != null) {
                    try {
                        String username = jwtService.extractUsername(token);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        if (jwtService.isTokenValid(token, userDetails)) {
                            // Create authentication token
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities());

                            // Set the authentication in the accessor
                            accessor.setUser(authentication);
                        }
                    } catch (Exception e) {
                        // Log error but don't block connection
                        System.err.println("WebSocket authentication failed: " + e.getMessage());
                    }
                }
            }
        }
        return message;
    }

    private String extractJwtFromCookies(String cookieHeader) {
        // Parse cookies and extract JWT cookie
        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            String[] parts = cookie.trim().split("=", 2);
            if (parts.length == 2 && "jwt".equals(parts[0])) {
                return parts[1];
            }
        }
        return null;
    }
}
