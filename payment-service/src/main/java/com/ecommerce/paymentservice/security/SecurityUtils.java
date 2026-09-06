package com.ecommerce.paymentservice.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? (String) authentication.getPrincipal() : null;
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public void assertOwnerOrAdmin(String resourceUserId) {
        String currentUserId = getCurrentUserId();
        if (!isAdmin() && (currentUserId == null || !currentUserId.equals(resourceUserId))) {
            throw new AccessDeniedException("You may only access your own payments");
        }
    }
}
