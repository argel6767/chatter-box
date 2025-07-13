package com.chat_room_app.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;


// JwtHandshakeInterceptor.java
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtService jwtService;
    @Autowired private UserDetailsService userDetailsService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        List<String> cookies = request.getHeaders().get(HttpHeaders.COOKIE);
        String token = extractJwtFromCookies(cookies);
        if (token != null) {
            String username = jwtService.extractUsername(token);
            UserDetails ud = userDetailsService.loadUserByUsername(username);

            if (!jwtService.isTokenValid(token, ud)) {
                throw new BadCredentialsException("JWT invalid/expired");
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            ud, null, ud.getAuthorities());

            // 1. Store in attributes so it lives for the entire socket
            attributes.put("authenticatedUser", auth);
            attributes.put("username", username);

            // 2. Expose it to Spring Security
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            attributes.put("SPRING_SECURITY_CONTEXT", context);
        }
        return true; // accept the handshake
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {}

    private String extractJwtFromCookies(List<String> cookieHeaders) {
        if (cookieHeaders == null) return null;
        for (String header : cookieHeaders) {
            for (String cookie : header.split(";")) {
                String[] p = cookie.trim().split("=", 2);
                if (p.length == 2 && "jwt".equals(p[0])) return p[1];
            }
        }
        return null;
    }
}
