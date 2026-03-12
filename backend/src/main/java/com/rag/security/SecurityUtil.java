package com.rag.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility to get the current authenticated user's ID and username from the security context.
 */
@Component
public class SecurityUtil {

    /**
     * Get the current authenticated user's ID (UUID string).
     * Returns null if not authenticated.
     */
    public String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof String userId) {
            return userId;
        }
        return null;
    }

    /**
     * Get the current authenticated user's username.
     */
    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof String username) {
            return username;
        }
        return null;
    }
}
