package com.second_project.book_store.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Thymeleaf controller for login page.
 * 
 * Spring Security handles the POST /login automatically.
 * This controller only handles GET /login to display the login form.
 * 
 * Flow:
 * - GET /login → Display login form
 * - User submits form → POST /login (handled by Spring Security)
 * - Spring Security validates credentials using CustomUserDetailsService
 * - On success → Redirect to default success URL (or configured URL)
 * - On failure → Redirect back to /login?error
 */
@Controller
public class LoginPageController {

    /**
     * Display login form.
     * 
     * Spring Security automatically handles:
     * - POST /login (form submission)
     * - Authentication logic
     * - Success/failure redirects
     * 
     * URL: http://127.0.0.1:8080/login
     * 
     * @param error Error parameter from Spring Security (if login failed)
     * @param logout Logout parameter (if user logged out)
     * @param model Model for Thymeleaf template
     * @return Thymeleaf template name
     */
    @GetMapping("/login")
    public String showLoginForm(String error, String logout, String redirect, Model model) {
        // Check for login error
        if (error != null) {
            model.addAttribute("error", "Invalid email or password. Please try again.");
        }
        
        // Check for logout success
        if (logout != null) {
            model.addAttribute("success", "You have been logged out successfully.");
        }

        // Store redirect URL if provided
        if (redirect != null && !redirect.isEmpty()) {
            model.addAttribute("redirect", redirect);
        }
        
        // Note: Success message from password reset is handled via flash attributes
        // (set in PasswordResetPageController)
        
        return "login";  // Thymeleaf template: src/main/resources/templates/login.html
    }
}

