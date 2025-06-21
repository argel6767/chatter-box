package com.chat_room_app.jwt;

import com.chat_room_app.users.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    /**
     * Gets the email of the currently authenticated user from the JWT token
     * @return The email of the authenticated user
     * @throws IllegalStateException if no user is authenticated
     */
    public static String getCurrentUserUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        return principal.toString();
    }

    /**
     * Gets the Id of the currently authenticated user
     * by casting the UserDetails object to User entity class
     * @return long
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            User user = (User) userDetails;
            return user.getId();
        }

        return -1L;
    }

    /**
     * Determines whether the currently authenticated user is a admin
     * @return whether a user has an admin role
     * @throws IllegalStateException if no user is authenticated or the use has no roles
     */
    public static boolean isUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new IllegalStateException();
        }

        boolean isAdmin = ((UserDetails) principal).getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        return isAdmin;
    }
}