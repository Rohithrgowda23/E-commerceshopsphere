package com.ecommerce.cartservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * The cart ALWAYS belongs to the authenticated caller — there is no
 * userId in the cart URL paths (unlike user-service's /{id}) precisely
 * so a user can never even attempt to address someone else's cart.
 */
@Component
public class SecurityUtils {

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? (String) authentication.getPrincipal() : null;
    }
}
