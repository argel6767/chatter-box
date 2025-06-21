package com.chat_room_app.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(HandlerExceptionResolver handlerExceptionResolver, JwtService jwtService, UserDetailsService userDetailsService) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }


    /**
     * filter requests to check if API requests are valid/contain the JWT Token
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // Extract JWT token from cookie instead of Authorization header
            Cookie[] cookies = request.getCookies();
            String jwt = null;

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt".equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        break;
                    }
                }
            }

            // If token exists, validate it
            if (jwt != null) {
                final String username = jwtService.extractUsername(jwt);
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (username != null && authentication == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(token);
                    }
                }
            }
            filterChain.doFilter(request, response);
        } catch (NullPointerException npe) {
            handlerExceptionResolver.resolveException(request, response, null, npe);
        }
    }
}
