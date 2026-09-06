package com.ecommerce.orderservice.security;

import com.ecommerce.orderservice.exception.ResourceNotFoundException;
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

    /**
     * Orders are only visible/actionable by their placing user, unless the
     * caller is an ADMIN. A non-owner gets a 404 rather than a 403 for
     * GET requests, so order existence isn't leaked to other users; for
     * mutating actions (cancel) a 403 is more honest since the order's
     * existence is implied by the caller having its id.
     */
    public void assertOwnerOrAdminForRead(String resourceUserId) {
        String currentUserId = getCurrentUserId();
        if (!isAdmin() && (currentUserId == null || !currentUserId.equals(resourceUserId))) {
            throw new ResourceNotFoundException("Order not found");
        }
    }

    public void assertOwnerOrAdminForWrite(String resourceUserId) {
        String currentUserId = getCurrentUserId();
        if (!isAdmin() && (currentUserId == null || !currentUserId.equals(resourceUserId))) {
            throw new AccessDeniedException("You may only modify your own orders");
        }
    }
}
