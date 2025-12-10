package com.second_project.book_store.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.second_project.book_store.config.properties.AdminProperties;
import com.second_project.book_store.security.CustomUserDetails;
import com.second_project.book_store.service.CartService;

/**
 * Global model attributes for all Thymeleaf page controllers.
 * 
 * This class automatically adds common attributes to all page templates,
 * avoiding the need to add them in every controller method.
 * 
 * BEST PRACTICES:
 * - Use @ControllerAdvice to apply to all controllers in specified packages
 * - Use @ModelAttribute to add attributes to all models
 * - Keep attributes minimal and commonly used
 * - Automatically populate cart count for authenticated users
 */
@ControllerAdvice(basePackages = "com.second_project.book_store.controller.page")
public class GlobalModelAttributes {

    private final AdminProperties adminProperties;
    private final CartService cartService;

    public GlobalModelAttributes(AdminProperties adminProperties, CartService cartService) {
        this.adminProperties = adminProperties;
        this.cartService = cartService;
    }

    /**
     * Add admin email to all page models for footer display.
     */
    @ModelAttribute("adminEmail")
    public String getAdminEmail() {
        return adminProperties.getEmail();
    }

    /**
     * Add cart item count to all pages for authenticated users.
     * 
     * This ensures the cart badge in the navbar shows the correct count
     * on every page without needing to add it manually in each controller.
     * 
     * Returns 0 for unauthenticated users or if cart is empty.
     */
    @ModelAttribute("cartItemCount")
    public Integer getCartItemCount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Check if user is authenticated and not anonymous
        if (authentication != null && 
            authentication.isAuthenticated() && 
            authentication.getPrincipal() instanceof CustomUserDetails) {
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return cartService.getCartItemCount(userDetails.getUserId());
        }
        
        return 0; // Return 0 for unauthenticated users
    }
}

